package eu.clarin.cmdi.vlo.config;

import javax.servlet.ServletContext;

/**
 * VLO configuration, taking context into account<be><br>
 * 
 * In addition to letting the packaged {@literal VloConfig.xml} determine
 * the value of the parameters in the VLO configuration, you can configure
 * a web application by means of an XML file that resides outside the 
 * package. By letting a parameter named<br><br>
 *
 * externalConfig<br><br>
 * 
 * in the context of the container in which the web application resides 
 * reference an XML file similar to the packaged one, the values of the
 * parameters defined in this file will override those of the packaged
 * parameters. Please note that the use of an external XML file is not
 * compulsory.<br><br>
 * 
 * Yet another way to externally configure the web application is to define
 * parameters by including them in the context fragment not via an XML file,
 * but directly. At the moment, only the packaged <br><br>
 * 
 * solrUrl<br><br>
 * 
 * parameter can be overridden in this way.
 * 
 * @author keeloo
 */
public class VloContextConfig extends VloConfig {
    
    /**
     * Switch to external configuration.<br><br>
     * 
     * @param config static configuration 
     *
     * @return the configuration
     */
    public static VloConfig switchToExternalConfig(ServletContext context) {
        
        if (config == null) {
            // the configuration is not there yet; create it now
            config = new VloConfig();
        }
       
        String fileName;

        // check for a reference to an external configuration file
        fileName = context.getInitParameter("externalConfig");
                
        if (fileName == null) {
            // no external configuration file
        } else {
            config = (VloConfig) read(fileName, config);
        }
        
        /**
         * In addition to modifications via an external configuration file,
         * check if the current configuration needs to be modified because of a
         * parameter defined in the context directly.
         */        
        String url = context.getInitParameter("solrUrl");
        
        if (url == null){
            // no overruling parameter in the context 
        } else
        {
            // overrule the current value of solrUrl

            VloConfig.setSolrUrl(url);
        }
        
        // return the current configuration, modified or not
        return (VloConfig)config;
    }
}
