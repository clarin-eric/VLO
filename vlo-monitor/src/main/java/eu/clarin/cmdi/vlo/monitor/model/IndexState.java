package eu.clarin.cmdi.vlo.monitor.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Entity
public class IndexState implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @OneToMany(targetEntity = FacetState.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<FacetState> facetStates;

    private Long totalRecordCount;

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

    public Long getTotalRecordCount() {
        return totalRecordCount;
    }

    public void setTotalRecordCount(Long totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    @Override
    public String toString() {
        return String.format("IndexState[id='%s', timestamp='%s', totalRecordCount=%d]", id, Objects.toString(timestamp), totalRecordCount);
    }

}
