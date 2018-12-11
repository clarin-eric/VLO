/**
 *
 */
package eu.clarin.cmdi.vlo.config;

import eu.clarin.cmdi.vlo.FieldKey;
import java.util.Map;

/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public final class FieldNameServiceImpl implements FieldNameService {

    private final Map<String, String> fields;
    private final Map<String, String> deprecatedField;

    public FieldNameServiceImpl(VloConfig vloConfig) {
        this.fields = vloConfig.getFields();
        this.deprecatedField = vloConfig.getDeprecatedFields();
    }

    @Override
    public String getFieldName(FieldKey key) {
        return fields.get(key.toString());
    }

    @Override
    public String getDeprecatedFieldName(FieldKey key) {
        return deprecatedField.get(key.toString());
    }
}
