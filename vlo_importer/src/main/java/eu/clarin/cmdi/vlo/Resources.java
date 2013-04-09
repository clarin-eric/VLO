package eu.clarin.cmdi.vlo;

/**
 * Definition of keys to message texts.<br><br>
 * 
 * Please note that value of a key defined in this class is defined in the
 * VloWebApplication.properties file. The key value pairs can be used in HTML
 * pages that are subject to Wicket, for example:<br><br>
 *
 * {@literal <div class="resourceList"><span>}
 * {@literal    <wicket:message key="resources">[Resources]</wicket:message>:</span>}
 * .
 * .
 * .
 * {@literal </div>}
 */
public interface Resources {
    
    public static final String FIELD = "field";
    public static final String NO_RESOURCE_FOUND = "noResourceFound";
    public static final String VALUE = "value";
    public static final String RESULTS = "results";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String OPEN_IN_ORIGINAL_CONTEXT = "openInOriginalContext";
    public static final String ORIGINAL_CONTEXT_NOT_AVAILABLE = "originalContextNotAvailable";  

    // keys to text used in labels in the page showing search result
    public static final String LANDING_PAGE = "landingPage";    
    public static final String LANDING_PAGES = "landingPages";    
}
