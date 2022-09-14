package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.AnonymizeUserEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.DomainEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.event.UserProcessedEvents;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.event.EventType.*;

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
    public void detectAnonymizeUserEvents() {
        /*System.out.println("Processing anonymize user events");
        ProcessedAnonymizeUserEvents lastProcessedEvent = processedAnonymizeUserEventsRepository.findAll().stream().findFirst().get();

        if(lastProcessedEvent == null) {*/
            /*creates the entry if it does not exist*/
         /*   lastProcessedEvent = new ProcessedAnonymizeUserEvents(0);
        }

        Set<AnonymizeUserEvent> events = eventRepository.getEvents("ANONYMIZE_USER", lastProcessedEvent.getLastProcessed());
        Integer newLastProcessedId = 0;
        for(AnonymizeUserEvent e : events) {
            tournamentFunctionalities.anonymizeUser(e.getUserAggregateId());
            newLastProcessedId = e.getId();
        }

        lastProcessedEvent.setLastProcessed(newLastProcessedId);
        processedAnonymizeUserEventsRepository.save(lastProcessedEvent);
*/
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
