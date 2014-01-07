/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarin.cmdi.vlo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.wicket.Session;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * VLO web page parameters 
 * 
 * @author keeloo
 */
public class VloPageParameters extends PageParameters {
    
    public VloPageParameters (){
        // needs to be here because of the other constructor
        super ();
        // add the theme parameter
        // ...
    }
    
    public VloPageParameters (PageParameters parameters){
        // store the parameters as VloPageParameters
    }
    
    public PageParameters convert (){
        
        PageParameters param;
        
        // needs to be implemented
        
        // present the page parameters in the old style
        
        param = null;
        
        return param;
    }

    /**
     * Take in  old style parameters. 
     * 
     * 
     * not being used at the moment; use code in the previous method
     * 
     * @param param
     * @return 
     */
    public static VloPageParameters convert (PageParameters param){
        List<NamedPair> entries = param.getAllNamed();
        
        // iterate over the set, and put entries in new page parameters 
        VloPageParameters VloPageParameters;
        VloPageParameters = new VloPageParameters ();
        for (Iterator<NamedPair> it = entries.iterator(); it.hasNext();) {
            NamedPair entry = it.next();
            VloPageParameters.add(entry.getKey(), entry.getValue());
        }
        
        return VloPageParameters;
    }

    /**
     * 
     * @param PersistentParameters parameters concerning theme and other 
     * non query related parameters. These parameters are to kept in the URL
     * during the session.
     */
    public void merge(PageParameters parameters) {   
        
        // get a themed session from the current thread 
        VloSession themedSession;
        themedSession = (VloSession)Session.get();
        
        // get the parameters associated with the session
        PageParameters persistentParameters;
        persistentParameters = themedSession.getVloSessionPageParameters();
        
        // merge the parameters with the persistent ones and return the result
        
        // ...
        
    }
}
