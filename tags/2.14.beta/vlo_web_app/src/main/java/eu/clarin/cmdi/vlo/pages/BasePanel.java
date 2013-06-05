
package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.VloWebApplication;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Objects with properties common to all VLO web application's panel objects
 * 
 * @author keeloo
 */
public class BasePanel extends Panel {
    
    // reference to the web application object
    static VloWebApplication webApp;
    
    /**
     * Make sure every web application object sends this message
     * 
     * @param vloWebApplication reference to the web application object
     */
    public static void setWebApp (VloWebApplication vloWebApplication){
        webApp = vloWebApplication;
    }
    
    // constructors 
    
    public BasePanel (String string, IModel<?> model){
        super(string, model);
    }
    
    public BasePanel (String string){
        super (string);
    }    
}
