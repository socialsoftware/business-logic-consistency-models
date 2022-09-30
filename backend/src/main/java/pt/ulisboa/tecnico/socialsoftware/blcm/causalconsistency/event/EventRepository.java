package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Set;
@Repository
@Transactional
public interface EventRepository extends JpaRepository<DomainEvent, Integer> {
    @Query(value = "select * from domain_events where type = :type and id > :minimumId" ,nativeQuery = true)
    Set<DomainEvent> getEvents(String type, Integer minimumId);

}
