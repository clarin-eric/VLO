package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.pages.FacetedSearchPage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 *
 * @see eu.clarin.cmdi.Start#main(String[])
 */
public class VloWicketApplication extends WebApplication implements ApplicationContextAware {

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
}
