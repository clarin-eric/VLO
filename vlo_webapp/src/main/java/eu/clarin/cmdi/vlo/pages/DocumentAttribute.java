/**
 * 
 */
package eu.clarin.cmdi.vlo.pages;

import java.io.Serializable;
import java.util.Collection;

public class DocumentAttribute implements Serializable{

    private static final long serialVersionUID = 1L;

    private String field;
    private String value = "";

    public DocumentAttribute(String field, Collection<Object> values) {
        this.field = field;
        if (value != null) {
            for (Object o : values) {
                if (!value.isEmpty()) {
                    this.value += ", ";
                }
                this.value += o.toString();
            }
        }
    }

    public String getField() {
        return field;
    }
    
    public String getValue() {
        return value;
    }

}