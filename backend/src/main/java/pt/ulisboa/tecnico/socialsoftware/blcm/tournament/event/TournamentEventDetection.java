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
    private TournamentService tournamentService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private TournamentProcessedEventsRepository tournamentProcessedEventsRepository;

    @Scheduled(cron = "*/10 * * * * *")
    public void detectAnonymizeUserEvents() {
        TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                .filter(e -> ANONYMIZE_USER.equals(e.getEventType()))
                .findFirst()
                .orElse(new TournamentProcessedEvents(ANONYMIZE_USER));

        Set<AnonymizeUserEvent> events = eventRepository.findAll()
                .stream()
                .filter(e -> ANONYMIZE_USER.equals(e.getType()))
                .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                .map(e -> (AnonymizeUserEvent) e)
                .collect(Collectors.toSet());

        for(AnonymizeUserEvent e : events) {
            handleAnonymizeUser(e);
        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        tournamentProcessedEvents.addProcessedEventsIds(processedEventsIds);
        tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
    }

    private void handleAnonymizeUser(AnonymizeUserEvent e) {
        AnonymizeUserEvent anonymizeUserEvent = e;
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();

        tournamentService.anonymizeUser(e.getUserAggregateId(), e.getName(), e.getUsername(), unitOfWork);

        unitOfWorkService.commit(unitOfWork);
    }
}

// TODO implement handlers for these events
/*e.getType().equals(ANONYMIZE_USER) ||
        e.getType().equals(REMOVE_COURSE_EXECUTION) ||
        e.getType().equals(UPDATE_COURSE_EXECUTION) ||
        e.getType().equals(REMOVE_USER))*/
