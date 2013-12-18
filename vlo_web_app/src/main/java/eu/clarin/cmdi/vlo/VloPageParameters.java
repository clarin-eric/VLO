/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarin.cmdi.vlo;

import java.util.Iterator;
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
    }
    
    public VloPageParameters (PageParameters parameters){
        // store the parameters as VloPageParameters
    }
    
    public org.apache.wicket.PageParameters convert (){
        
        org.apache.wicket.PageParameters param;
        
        // needs to be implemented
        
        param = null;
        
        return param;
    }

    /**
     * 
     * @param param
     * @return 
     */
    public VloPageParameters convert (org.apache.wicket.PageParameters param){
        
        // move page parameters in array of objects
        Set <Map.Entry<String,Object>> entries;
        entries = param.entrySet();
        
        // iterate over the set, and put entries in new page parameters 
        VloPageParameters VloPageParameters;
        VloPageParameters = new VloPageParameters ();
        for (Iterator<Map.Entry<String, Object>> it = entries.iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            VloPageParameters.add(entry.getKey(), entry.getValue());
        }
        
        return VloPageParameters;
    }

    /**
     * Add this, VLO page parameters, to the parameters that are already 
     * associated with the session
     * 
     */
    public void addToSession() {        
        
        // get a themed session from the current thread 
        VloSession themedSession;
        themedSession = (VloSession)Session.get();
        
        // store the parameters in the session object
        themedSession.addVloSessionPageParameters (this);
    }
}
