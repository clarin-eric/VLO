package eu.clarin.cmdi.vlo.exposure.frontend;

import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.Application;

import eu.clarin.cmdi.vlo.exposure.frontend.Record;
import eu.clarin.cmdi.vlo.exposure.frontend.HomePage;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see eu.clarin.cmdi.Start#main(String[])
 */
public class WicketApplication extends WebApplication {
    private VloConfig vloConfig;
    private final String configProperties = "./vlo-exposure-frontend/config.properties"; //System.getProperty("user.dir")+
    
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
            final Properties properties = new Properties();

            System.out.println(this.configProperties);
            properties.load(new FileReader(this.configProperties));
            final File configUrl = new File(properties.getProperty("vlo.config.file"));
            final XmlVloConfigFactory configFactory = new XmlVloConfigFactory(configUrl.toURI().toURL());
            this.vloConfig = configFactory.newConfig();
        }catch(Exception ex) {
            //logger.error(ex.getMessage());
            System.out.print(ex.getMessage());
        }
        
    }
    
    public static WicketApplication get() {
        return (WicketApplication) Application.get();
    }

    public VloConfig getConfig() {
        return this.vloConfig;
    }
    
    private void mountPages() {
        // frontend
        mountPage("/index", HomePage.class);
        mountPage("/record", Record.class);

    }
}
