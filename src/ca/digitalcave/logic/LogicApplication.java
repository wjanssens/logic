package ca.digitalcave.logic;

import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

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
import ca.digitalcave.logic.resource.SolverResource;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class LogicApplication extends Application {
	
	private static final Version FM_VERSION = new Version(2, 3, 21);
	private final Properties properties = new Properties();
	private final MappingJsonFactory jsonFactory = new MappingJsonFactory(new ObjectMapper());
	private final Configuration fmConfig = new Configuration(FM_VERSION);

	@Override
	public Restlet createInboundRoot() {
		final Router router = new Router();
		router.attach("/", new Redirector(getContext(), "index.html"));
		router.attach("/solver", SolverResource.class);
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
		
		fmConfig.setServletContextForTemplateLoading(servletContext, "/");
		fmConfig.setDefaultEncoding("UTF-8");
		fmConfig.setLocalizedLookup(true);
		fmConfig.setLocale(Locale.ENGLISH);
		fmConfig.setTemplateUpdateDelay(0);
		fmConfig.setObjectWrapper(new BeansWrapperBuilder(FM_VERSION).build());
		fmConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		
		super.start();
	}
	
	public ObjectMapper getObjectMapper() {
		return jsonFactory.getCodec();
	}
	
	public MappingJsonFactory getJsonFactory() {
		return jsonFactory;
	}
	
	public Configuration getFmConfig() {
		return fmConfig;
	}
	
	public Properties getProperties() {
		return properties;
	}

}
