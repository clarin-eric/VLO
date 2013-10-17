
package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.FacetConstants;
import java.io.Serializable;
import java.util.Collection;

/*
 * Representation of facet values used in the show results page class
 */
public class DocumentAttribute implements Serializable {

    private static final long serialVersionUID = 1L;
    private String field;
    private String value = "";

    /**
     * Create the representation of the value of a facet.
     * 
     * The representation is stored in the 'value' field. 
     *
     * @param field the name of the facet to be represented
     * @param values structure in which the values of a facet are stored
     */
    public DocumentAttribute(String field, Collection<Object> values) {
        this.field = field;
        if (value != null) {
            // represent the values in the structure associated with a facet
            for (Object o : values) {
                if (!value.isEmpty()) {
                    if (field.equals(FacetConstants.FIELD_DESCRIPTION)) {
                        // put values in the structure on seperate lines
                        this.value += "\n";
                    } else {
                        // separate values in the structure by commas
                        this.value += ", ";
                    }
                }
                this.value += o.toString();
            }
        }
    }

    /**
     * Get method for the name of the facet
     * 
     * @return 
     */
    public String getField() {
        return field;
    }

    /**
     * Get method for the value of the facet
     * 
     * @return
     */
    public String getValue() {
        return value;
    }
}