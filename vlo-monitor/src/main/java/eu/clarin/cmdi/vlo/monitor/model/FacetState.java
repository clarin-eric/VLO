package eu.clarin.cmdi.vlo.monitor.model;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Entity
public class FacetState implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String facet;

    private String val;

    private Long count;

    public FacetState() {
    }

    public FacetState(String facet, String val, Long count) {
        super();
        this.facet = facet;
        this.val = val;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFacet() {
        return facet;
    }

    public void setFacet(String facet) {
        this.facet = facet;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return String.format("FacetState[facet='%s', value='%s', count=%d]", facet, val, count);
    }

}
