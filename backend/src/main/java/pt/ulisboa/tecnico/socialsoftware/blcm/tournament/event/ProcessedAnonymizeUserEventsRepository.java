package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ProcessedAnonymizeUserEventsRepository extends JpaRepository<ProcessedAnonymizeUserEvents, Integer> {
}
