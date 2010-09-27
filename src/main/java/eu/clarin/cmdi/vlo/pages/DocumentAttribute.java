/**
 * 
 */
package eu.clarin.cmdi.vlo.pages;

import java.io.Serializable;

public class DocumentAttribute implements Serializable{

    private static final long serialVersionUID = 1L;

    private String field;
    private String value = "";

    public DocumentAttribute(String field, Object value) {
        this.field = field;
        if (value != null) {
            this.value = value.toString();
        }
    }

    public String getField() {
        return field;
    }
    
    public String getValue() {
        return value;
    }

}