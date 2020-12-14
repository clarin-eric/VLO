package eu.clarin.cmdi.vlo.monitor;

import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Repository
public interface IndexStateRepository extends CrudRepository<IndexState, Long> {
    IndexState findById(long id);
}
