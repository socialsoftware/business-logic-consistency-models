package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
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
    private TournamentRepository tournamentRepository;

    @Autowired
    private TournamentFunctionalities tournamentFunctionalities;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private TournamentProcessedEventsRepository tournamentProcessedEventsRepository;

    /* fixed delay guarantees this task only runs 10 seconds after the previous finished. With fixed dealy concurrent executions are not possible.*/
    @Scheduled(fixedDelay = 10000)
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
        Set<Integer> tournamentsIds = tournamentRepository.findAllNonDeleted().stream()
                .map(Tournament::getAggregateId)
                .collect(Collectors.toSet());

        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        for (Integer tournamentsId : tournamentsIds) {
            tournamentService.anonymizeUser(e.getUserAggregateId(), tournamentsId, e.getName(), e.getUsername(), unitOfWork);
        }

        unitOfWorkService.commit(unitOfWork);
    }



    @Scheduled(fixedDelay = 10000)
    public void detectRemoveCourseExecutionEvents() {
        TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                .filter(e -> REMOVE_COURSE_EXECUTION.equals(e.getEventType()))
                .findFirst()
                .orElse(new TournamentProcessedEvents(ANONYMIZE_USER));

        Set<RemoveCourseExecutionEvent> events = eventRepository.findAll()
                .stream()
                .filter(e -> REMOVE_COURSE_EXECUTION.equals(e.getType()))
                .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                .map(e -> (RemoveCourseExecutionEvent) e)
                .collect(Collectors.toSet());

        for(RemoveCourseExecutionEvent e : events) {
            handleRemoveCourseExecution(e);
        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        tournamentProcessedEvents.addProcessedEventsIds(processedEventsIds);
        tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
    }

    private void handleRemoveCourseExecution(RemoveCourseExecutionEvent e) {
        Set<Integer> tournamentsAggregateIds = tournamentRepository.findAllNonDeleted().stream()
                        .map(Tournament::getAggregateId)
                        .collect(Collectors.toSet());

        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        tournamentsAggregateIds.forEach(id -> {
            tournamentService.removeCourseExecution(id, e.getCourseExecutionId(), unitOfWork);
        });
        unitOfWorkService.commit(unitOfWork);
    }

    @Scheduled(fixedDelay = 10000)
    public void detectRemoveUserEvents() {
        TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                .filter(e -> REMOVE_USER.equals(e.getEventType()))
                .findFirst()
                .orElse(new TournamentProcessedEvents(ANONYMIZE_USER));

        Set<RemoveUserEvent> events = eventRepository.findAll()
                .stream()
                .filter(e -> REMOVE_USER.equals(e.getType()))
                .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                .map(e -> (RemoveUserEvent) e)
                .collect(Collectors.toSet());

        for(RemoveUserEvent e : events) {
            handleRemoveUser(e);
        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        tournamentProcessedEvents.addProcessedEventsIds(processedEventsIds);
        tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
    }

    private void handleRemoveUser(RemoveUserEvent e) {
        Set<Integer> tournamentsAggregateIds = tournamentRepository.findAllNonDeleted().stream()
                .map(Tournament::getAggregateId)
                .collect(Collectors.toSet());

        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        tournamentsAggregateIds.forEach(aggregateId -> {
            tournamentService.removeUser(aggregateId, e.getAggregateId(), unitOfWork);
        });
        unitOfWorkService.commit(unitOfWork);
    }

}

// TODO implement handlers for these events
/*
    e.getType().equals(REMOVE_USER))
*/
