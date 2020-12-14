package eu.clarin.cmdi.vlo.monitor.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
    
    private String value;
    
    private Integer count;

}
