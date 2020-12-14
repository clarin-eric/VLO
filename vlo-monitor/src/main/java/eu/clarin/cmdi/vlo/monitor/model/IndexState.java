package eu.clarin.cmdi.vlo.monitor.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
    
    @OneToMany(targetEntity=FacetState.class, fetch=FetchType.EAGER)
    private List<FacetState> facetStates;

    public Long getId() {
        return id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public List<FacetState> getFacetStates() {
        return facetStates;
    }
    
    

}
