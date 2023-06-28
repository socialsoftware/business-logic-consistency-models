package pt.ulisboa.tecnico.socialsoftware.ms.execution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.user.event.publish.RemoveUserEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.service.CourseExecutionService;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CourseExecutionEventHandling {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private CourseExecutionEventProcessing courseExecutionEventProcessing;
    @Autowired
    private CourseExecutionService courseExecutionService;
    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    /*
        USER_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleRemoveUserEvents() {
        Set<Integer> aggregateIds = courseExecutionRepository.findAll().stream().map(CourseExecution::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            CourseExecution courseExecution = courseExecutionRepository.findLastVersion(aggregateId).orElse(null);
            if (courseExecution == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = courseExecutionService.getEventSubscriptions(courseExecution.getAggregateId(), courseExecution.getVersion(), RemoveUserEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<RemoveUserEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(RemoveUserEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (RemoveUserEvent eventToProcess : eventsToProcess) {
                    courseExecutionEventProcessing.processRemoveUserEvent(aggregateId, eventToProcess);
                }
            }
        }
    }
}
