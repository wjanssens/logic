package ca.digitalcave.logic;

import java.io.InputStream;
import java.sql.Connection;
import java.util.Locale;
import java.util.Properties;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;

import ca.digitalcave.logic.resource.DefaultResource;
import ca.digitalcave.logic.resource.ResourceResource;
import ca.digitalcave.logic.resource.ScenariosResource;
import ca.digitalcave.logic.resource.TypeResource;
import ca.digitalcave.logic.util.PasswordUtil;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class LogicApplication extends Application {
	
	private static final Version FM_VERSION = new Version(2, 3, 21);
	private final Properties properties = new Properties();
	private final MappingJsonFactory jsonFactory = new MappingJsonFactory(new ObjectMapper());
	private final Configuration fmConfig = new Configuration(FM_VERSION);
	private SqlSessionFactory sqlSessionFactory;

	@Override
	public Restlet createInboundRoot() {
		final Router router = new Router();
		router.attach("/", new Redirector(getContext(), "index.html"));
		router.attach("/scenarios", ScenariosResource.class);
		router.attach("/scenarios/{scenario}/types", TypeResource.class);
		router.attach("/scenarios/{scenario}/types/{type}/resources", ResourceResource.class);
		router.attachDefault(DefaultResource.class);
		return router;
	}
	
	@Override
	public synchronized void start() throws Exception {
		final Object servletContext = getContext().getAttributes().get("org.restlet.ext.servlet.ServletContext");

		jsonFactory.setCodec(new ObjectMapper());
		
		final String resource = "war:///WEB-INF/config.properties";
		final InputStream is = Context.getCurrent().getClientDispatcher().handle(new Request(Method.GET, resource)).getEntity().getStream();
		properties.load(is);
		
		properties.put("db.password", PasswordUtil.deobfuscate(properties.getProperty("db.password")));

		fmConfig.setServletContextForTemplateLoading(servletContext, "/");
		fmConfig.setDefaultEncoding("UTF-8");
		fmConfig.setLocalizedLookup(true);
		fmConfig.setLocale(Locale.ENGLISH);
		fmConfig.setTemplateUpdateDelay(0);
		fmConfig.setObjectWrapper(new BeansWrapperBuilder(FM_VERSION).build());
		fmConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		
		org.apache.ibatis.logging.LogFactory.useJdkLogging();
		
		final PooledDataSource dataSource = new PooledDataSource(
				properties.getProperty("db.driver"), 
				properties.getProperty("db.url"), 
				properties.getProperty("db.username"), 
				properties.getProperty("db.password"));
		dataSource.setPoolPingQuery(properties.getProperty("db.validationQuery"));
		final Environment environment = new Environment("scheduler", new JdbcTransactionFactory(), dataSource);
		final org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration(environment);
		configuration.addMappers("ca.digitalcave.scheduler.data");
		final SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
		sqlSessionFactory = sqlSessionFactoryBuilder.build(configuration);

		migrate();

		super.start();
	}
	
	public ObjectMapper getObjectMapper() {
		return jsonFactory.getCodec();
	}
	
	public MappingJsonFactory getJsonFactory() {
		return jsonFactory;
	}
	
	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}
	
	public Configuration getFmConfig() {
		return fmConfig;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	private void migrate() throws Exception {
		final String schema = getProperties().getProperty("db.schema");
		final String context = getProperties().getProperty("db.context", "prod");

		final SqlSession session = sqlSessionFactory.openSession();
		try {
			final Connection conn = session.getConnection();
			final Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
			final ResourceAccessor ra = new ClassLoaderResourceAccessor();
			final Liquibase l = new Liquibase("ca/digitalcave/data/changelog.xml", ra, db);
			if (schema != null && schema.trim().length() > 0) {
				l.getDatabase().setDefaultSchemaName(schema);
			}
			l.update(context);
		} finally {
			session.close();
		}
	}
}
