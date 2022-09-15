package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.AnonymizeUserEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.DomainEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.*;

@Component
public class TournamentEventDetection {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TournamentFunctionalities tournamentFunctionalities;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private TournamentProcessedEventsRepository tournamentProcessedEventsRepository;

    @Scheduled(cron = "*/10 * * * * *")
    @Transactional
    public void detectTournamentEvents() {
        TournamentProcessedEvents lastProcessedEvent = tournamentProcessedEventsRepository.findAll().stream()
                .findFirst()
                .orElse(new TournamentProcessedEvents(0));

        Set<DomainEvent> events = eventRepository.findAll()
                .stream()
                .filter(e -> e.getId() > lastProcessedEvent.getLastProcessedEventId())
                .filter(e ->
                        e.getType().equals(ANONYMIZE_USER) ||
                                e.getType().equals(REMOVE_COURSE_EXECUTION) ||
                                e.getType().equals(UPDATE_COURSE_EXECUTION) ||
                        e.getType().equals(REMOVE_USER))
                .map(e -> (RemoveCourseExecutionEvent) e)
                .collect(Collectors.toSet());

        for(DomainEvent e : events) {
            switch (e.getType()) {
                case ANONYMIZE_USER:
                    handleAnonymizeUser((AnonymizeUserEvent) e);
                    break;
                case REMOVE_COURSE_EXECUTION:
                    break;
                case REMOVE_USER:
                    break;
                case UPDATE_COURSE_EXECUTION:
                    break;
            }
        }

        Integer newLastProcessedId = events.stream().map(DomainEvent::getId).max(Integer::compareTo).orElse(lastProcessedEvent.getLastProcessedEventId());
        lastProcessedEvent.setLastProcessedEventId(newLastProcessedId);
        tournamentProcessedEventsRepository.save(lastProcessedEvent);
    }

    private void handleAnonymizeUser(AnonymizeUserEvent e) {
        AnonymizeUserEvent anonymizeUserEvent = e;
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();

        tournamentService.anonymizeUser(e.getUserAggregateId(), e.getName(), e.getUsername(), unitOfWork);

        unitOfWorkService.commit(unitOfWork);
    }
}
