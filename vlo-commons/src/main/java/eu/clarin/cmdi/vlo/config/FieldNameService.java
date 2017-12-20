/**
 * 
 */
package eu.clarin.cmdi.vlo.config;

import eu.clarin.cmdi.vlo.FieldKey;

/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public interface FieldNameService {
    
    /**
     * @param key
     * @return valid fieldname in the database (solr)
     */
    public String getFieldName(FieldKey key);
    /**
     * @param key
     * @return deprecated fieldname in the database (solr)
     */
    public String getDeprecatedFieldName(FieldKey key);
}
