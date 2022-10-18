package pt.ulisboa.tecnico.socialsoftware.blcm.execution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.RemoveUserEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEvents;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEventsRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.REMOVE_USER;

public class CourseExecutionEventDetection {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProcessedEventsRepository processedEventsRepository;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private CourseExecutionService courseExecutionService;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    /*
        USER_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectRemoveUserEvents() {
        Set<Integer> executionAggregateIds = courseExecutionRepository.findAll().stream().map(CourseExecution::getAggregateId).collect(Collectors.toSet());
        List<Event> events = getEmittedEvents(REMOVE_USER);
        for(Integer executionAggregateId : executionAggregateIds) {
            ProcessedEvents processedEvents = getTournamentProcessedEvents(REMOVE_USER, executionAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processRemoveUserEvents(executionAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processRemoveUserEvents(Integer executionAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<RemoveUserEvent> removeUserEvents = events.stream()
                .map(e -> RemoveUserEvent.class.cast(e))
                .collect(Collectors.toSet());
        for(RemoveUserEvent e : removeUserEvents) {
            Set<Integer> tournamentIdsByUser = courseExecutionRepository.findAllAggregateIdsByUser(e.getAggregateId());
            if(!tournamentIdsByUser.contains(executionAggregateId)) {
                continue;
            }
            System.out.printf("Processing remove user %d event for course execution %d\n", e.getAggregateId(), executionAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            CourseExecution updatedExecution = courseExecutionService.removeUser(executionAggregateId, e.getAggregateId(), e.getAggregateVersion(), unitOfWork);
            if(updatedExecution != null) {
                updatedExecution.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());

        }
        return newlyProcessedEventVersions;
    }

    /*
        COURSE_EXISTS
     */

    private List<Event> getEmittedEvents(String eventType) {
        return eventRepository.findAll()
                .stream()
                .filter(e -> eventType.equals(e.getType()))
                .distinct()
                .sorted(Comparator.comparing(Event::getTs).reversed())
                .collect(Collectors.toList());
    }

    private ProcessedEvents getTournamentProcessedEvents(String eventType, Integer tournamentAggregateId) {
        return processedEventsRepository.findAll().stream()
                .filter(pe -> tournamentAggregateId.equals(pe.getAggregateId()))
                .filter(pe -> eventType.equals(pe.getEventType()))
                .findFirst()
                .orElse(new ProcessedEvents(eventType, tournamentAggregateId));
    }
}
