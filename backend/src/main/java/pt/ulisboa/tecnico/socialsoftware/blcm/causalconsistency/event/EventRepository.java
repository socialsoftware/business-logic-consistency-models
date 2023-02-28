package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
}
