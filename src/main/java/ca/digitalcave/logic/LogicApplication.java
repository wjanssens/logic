package ca.digitalcave.logic;

import ca.digitalcave.logic.resource.DefaultResource;
import ca.digitalcave.logic.resource.SolverResource;
import com.fasterxml.jackson.core.JsonFactory;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;

import java.util.Locale;
import java.util.Properties;

public class LogicApplication extends Application {
	
	private static final Version FM_VERSION = new Version(2, 3, 21);
	private final Properties properties = new Properties();
	private final JsonFactory jsonFactory = new JsonFactory();
	private final Configuration fmConfig = new Configuration(FM_VERSION);

	public static void main(String[] args) throws Exception {
		final Component component = new Component();
		component.getServers().add(Protocol.HTTP, 8182);
		component.getDefaultHost().attach(new LogicApplication());
		component.start();
	}

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
		/*
		final String resource = "war:///WEB-INF/config.properties";
		final InputStream is = getClass().getResourceAsStream("config.properties");
		properties.load(is);
		*/

		fmConfig.setClassForTemplateLoading(getClass(), "/");
		fmConfig.setDefaultEncoding("UTF-8");
		fmConfig.setLocalizedLookup(true);
		fmConfig.setLocale(Locale.ENGLISH);
		fmConfig.setTemplateUpdateDelay(0);
		fmConfig.setObjectWrapper(new BeansWrapperBuilder(FM_VERSION).build());
		fmConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		super.start();
	}
	
	public JsonFactory getJsonFactory() {
		return jsonFactory;
	}
	
	public Configuration getFmConfig() {
		return fmConfig;
	}
	
	public Properties getProperties() {
		return properties;
	}

}
