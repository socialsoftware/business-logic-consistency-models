package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ProcessedEventsRepository extends JpaRepository<ProcessedEvents, Integer> {
}
