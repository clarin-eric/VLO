/**
 * 
 */
package eu.clarin.cmdi.vlo.config;

import eu.clarin.cmdi.vlo.FieldKey;

/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public final class FieldNameServiceImpl implements FieldNameService{
    
    private VloConfig vloConfig;
    

    public FieldNameServiceImpl(VloConfig vloConfig) {
        this.vloConfig = vloConfig;
    }
    @Override
    public String getFieldName(FieldKey key) {
        return this.vloConfig.getFields().get(key.toString());
    }
    public String getDeprecatedFieldName(FieldKey key) {
        return this.vloConfig.getDeprecatedFields().get(key.toString());
    }
}
