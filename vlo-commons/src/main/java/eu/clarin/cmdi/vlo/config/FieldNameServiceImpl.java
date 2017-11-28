/**
 * 
 */
package eu.clarin.cmdi.vlo.config;

import eu.clarin.cmdi.vlo.FacetConstants.KEY;

/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public class FieldNameServiceImpl implements FieldNameService{
    
    private VloConfig vloConfig;
    

    public FieldNameServiceImpl() {
        try {
            this.vloConfig = new DefaultVloConfigFactory().newConfig();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public FieldNameServiceImpl(VloConfig vloConfig) {
        this.vloConfig = vloConfig;
    }
    @Override
    public String getFieldName(KEY key) {
        return this.vloConfig.getFields().get(key.toString());
    }
}
