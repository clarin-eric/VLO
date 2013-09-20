
package eu.clarin.cmdi.vlo;

/**
 * Definition of keys to message texts.
 * 
 * Please note that value of a key defined in this class is defined in the
 * VloWebApplication.properties file. The key value pairs can be used in HTML
 * pages that are subject to Wicket, for example:<br><br>
 *
 * {@literal <div class="resourceList"><span>}<br>
 * .<br>
 * .<br>
 * {@literal <wicket:message key="resources">}<br>
 * {@literal [Resources]}<br>
 * {@literal </wicket:message>:</span>}<br>
 * .<br>
 * .<br>
 * {@literal </div>}
 */
public interface Resources {
    
    public static final String FIELD = "field";
    public static final String NO_RESOURCE_FOUND = "noResourceFound";
    public static final String RESOURCE = "resource";
    public static final String RESOURCE_PL = "resource.PL";
    public static final String VALUE = "value";
    public static final String RESULTS = "results";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String OPEN_IN_ORIGINAL_CONTEXT = "openInOriginalContext";
    public static final String ORIGINAL_CONTEXT_NOT_AVAILABLE = "originalContextNotAvailable";  
    
    /** Key to a text on the result page labeling a single landing page. */
    public static final String LANDING_PAGE = "landingPage";    
    /** Key to a text on the result page labeling s list of landing pages. */
    public static final String LANDING_PAGES = "landingPages";    
    /** Key to a text on the result page labeling a single search page. */
    public static final String SEARCH_PAGE = "searchPage";    
    /** Key to a text on the result page labeling a list of search pages.*/
    public static final String SEARCH_PAGES = "searchPages";    
}
