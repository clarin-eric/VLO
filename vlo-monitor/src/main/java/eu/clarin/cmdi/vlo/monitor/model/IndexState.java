package eu.clarin.cmdi.vlo.monitor.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Entity
public class IndexState implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Date timestamp;

    @OneToMany(targetEntity = FacetState.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<FacetState> facetStates;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<FacetState> getFacetStates() {
        return facetStates;
    }

    public void setFacetStates(List<FacetState> facetStates) {
        this.facetStates = facetStates;
    }

    @Override
    public String toString() {
        return String.format("IndexState[id='%s', timestamp='%s']", id, Objects.toString(timestamp));
    }

}
