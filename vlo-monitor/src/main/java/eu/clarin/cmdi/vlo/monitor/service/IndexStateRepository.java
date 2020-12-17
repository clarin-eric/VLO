package eu.clarin.cmdi.vlo.monitor.service;

import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import java.util.Date;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Repository
public interface IndexStateRepository extends CrudRepository<IndexState, Long> {
    IndexState findById(long id);
    
    Optional<IndexState> findFirstByOrderByTimestampDesc();
    
    @Query("SELECT i FROM IndexState i where i.timestamp < ?1")
    Iterable<IndexState> findOlderThan(Date maxTimestamp);
}
