package eu.clarin.cmdi.vlo.exposure.frontend;

import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import eu.clarin.cmdi.vlo.exposure.frontend.service.FrontEndDataProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.Application;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 *
 * @see eu.clarin.cmdi.Start#main(String[])
 */
public class WicketApplication extends WebApplication {

    private VloConfig vloConfig;
    private final String configProperties = "./vlo-exposure-frontend/config.properties"; //System.getProperty("user.dir")+
    private FrontEndDataProvider frontEndDataProvider;
    private final static Logger logger = LoggerFactory.getLogger(WicketApplication.class);

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends WebPage> getHomePage() {
        return HomePage.class;
    }

    /**
     * @see org.apache.wicket.Application#init()
     */
    @Override
    public void init() {
        super.init();
        initVloConfig();
        mountPages();
    }

    private void initVloConfig() {
        try {

            final Optional<String> configLocation
                    = Optional.ofNullable(
                            getServletContext().getInitParameter("eu.carlin.cmdi.vlo.config.location"));

            final File configFile = configLocation.map(File::new).orElseGet(() -> {
                final Properties properties = new Properties();
                try (FileReader fr = new FileReader(configProperties)) {
                    properties.load(fr);
                    return new File(properties.getProperty("vlo.config.file"));
                } catch (IOException ex) {
                    throw new RuntimeException("Could not read properties file " + configProperties, ex);
                }
            });

            final XmlVloConfigFactory configFactory = new XmlVloConfigFactory(configFile.toURI().toURL());
            this.vloConfig = configFactory.newConfig();
            this.frontEndDataProvider = new FrontEndDataProvider(vloConfig);
        } catch (IOException ex) {
            logger.error("Failed to initialise", ex);
        }

    }

    public static WicketApplication get() {
        return (WicketApplication) Application.get();
    }

    public VloConfig getConfig() {
        return this.vloConfig;
    }

    public FrontEndDataProvider getFrontendDataProvider(){
        return this.frontEndDataProvider;
    }
    private void mountPages() {
        // frontend
        mountPage("/index", HomePage.class);
        mountPage("/record", Record.class);

    }
}
