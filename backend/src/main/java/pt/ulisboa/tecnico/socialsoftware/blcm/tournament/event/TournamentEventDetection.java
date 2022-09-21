package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipant;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;

import java.sql.SQLException;
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
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
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
            Set<Integer> tournamentsAggregateIds = tournamentRepository.findAllNonDeleted().stream()
                    .filter(t -> e.getUserAggregateId().equals(t.getCreator().getAggregateId()) || t.getParticipants().stream().map(TournamentParticipant::getAggregateId).collect(Collectors.toSet()).contains(e.getUserAggregateId()))
                    .map(Tournament::getAggregateId)
                    .collect(Collectors.toSet());

            for (Integer tournamentsId : tournamentsAggregateIds) {
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.anonymizeUser(e.getUserAggregateId(), tournamentsId, e.getName(), e.getUsername(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);
            }
        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        tournamentProcessedEvents.addProcessedEventsIds(processedEventsIds);
        tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
    }


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
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
            Set<Integer> tournamentsAggregateIds = tournamentRepository.findAllNonDeleted().stream()
                    .filter(t -> e.getCourseExecutionAggregateId().equals(t.getCourseExecution().getAggregateId()))
                    .map(Tournament::getAggregateId)
                    .collect(Collectors.toSet());

            for (Integer tournamentAggregateId : tournamentsAggregateIds) {
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.removeCourseExecution(tournamentAggregateId, e.getCourseExecutionAggregateId(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);
            }


        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        tournamentProcessedEvents.addProcessedEventsIds(processedEventsIds);
        tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
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
            Set<Integer> tournamentsAggregateIds = tournamentRepository.findAllNonDeleted().stream()
                    .filter(t -> e.getUserAggregateId().equals(t.getCreator().getAggregateId()) || t.getParticipants().stream().map(TournamentParticipant::getAggregateId).collect(Collectors.toSet()).contains(e.getUserAggregateId()))
                    .map(Tournament::getAggregateId)
                    .collect(Collectors.toSet());

            tournamentsAggregateIds.forEach(aggregateId -> {
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.removeUser(aggregateId, e.getUserAggregateId(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);
            });
        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        tournamentProcessedEvents.addProcessedEventsIds(processedEventsIds);
        tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
    }



}