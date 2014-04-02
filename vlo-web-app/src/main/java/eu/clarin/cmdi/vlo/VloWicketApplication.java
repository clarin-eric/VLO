package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.service.XmlTransformationService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.pages.AllFacetValuesPage;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 *
 * @see eu.clarin.cmdi.Start#main(String[])
 */
public class VloWicketApplication extends WebApplication implements ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(VloWicketApplication.class);

    @Autowired
    private SolrDocumentService documentService;
    @Autowired
    private XmlTransformationService cmdiTransformationService;
    @Autowired
    private VloConfig vloConfig;

    private ApplicationContext applicationContext;

    /**
     * @return the home page of this application
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends WebPage> getHomePage() {
        return FacetedSearchPage.class;
    }

    /**
     * @see org.apache.wicket.Application#init()
     */
    @Override
    public void init() {
        super.init();
        // this listener will inject any spring beans that need to be autowired
        getComponentInstantiationListeners().add(new SpringComponentInjector(this, applicationContext));
        // register the resource of field names (used by eu.clarin.cmdi.vlo.wicket.componentsSolrFieldNameLabel)
        getResourceSettings().getStringResourceLoaders().add(new BundleStringResourceLoader("fieldNames"));
        // register the resource of resource type class properties
        getResourceSettings().getStringResourceLoaders().add(new BundleStringResourceLoader("resourceTypeClasses"));
        // register the resource of application properties (version information filtered at build time)
        getResourceSettings().getStringResourceLoaders().add(new BundleStringResourceLoader("application"));

        // Record (query result) page. E.g. /vlo/record?docId=abc123
        // (cannot encode docId in path because it contains a slash)
        mountPage("/record", RecordPage.class);
        // All facet values page (kept for compatibility with old bookmarks)
        // E.g. /vlo/values/genre?facetMinOccurs=1 (min occurs not in path 
        // because it's a filter on the facet list)
        mountPage("/values/${" + AllFacetValuesPage.SELECTED_FACET_PARAM + "}", AllFacetValuesPage.class);
        
        // configure cache by applying the vlo configuration settings to it
        final int pagesInApplicationCache = vloConfig.getPagesInApplicationCache();
        logger.info("Setting Wicket in-memory cache size to {}", pagesInApplicationCache);
        this.getStoreSettings().setInmemoryCacheSize(pagesInApplicationCache);

        final Bytes sessionCacheSize = Bytes.kilobytes((long) vloConfig.getSessionCacheSize());
        logger.info("Setting Wicket max size per session to {}", sessionCacheSize);
        this.getStoreSettings().setMaxSizePerSession(sessionCacheSize);
    }

    /**
     *
     * @return the active VLO wicket application
     */
    public static VloWicketApplication get() {
        return (VloWicketApplication) Application.get();
    }

    /**
     * Method needed for dynamic injection of application context (as happens in
     * unit tests)
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     *
     * @return a service that retrieves SolrDocuments from the attached index
     */
    public SolrDocumentService getDocumentService() {
        return documentService;
    }

    /**
     *
     * @return a service that transforms CMDI documents to HTML representations
     */
    public XmlTransformationService getCmdiTransformationService() {
        return cmdiTransformationService;
    }

}
