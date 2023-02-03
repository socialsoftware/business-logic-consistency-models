package pt.ulisboa.tecnico.socialsoftware.blcm.execution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.RemoveUserEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.repository.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionFunctionalities;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CourseExecutionEventDetection {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CourseExecutionFunctionalities courseExecutionFunctionalities;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    /*
        USER_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectRemoveUserEvents() {
        Set<Integer> aggregateIds = courseExecutionRepository.findAll().stream().map(CourseExecution::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            CourseExecution courseExecution = courseExecutionRepository.findLastQuestionVersion(aggregateId).orElse(null);
            if (courseExecution == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = courseExecution.getEventSubscriptionsByEventType(RemoveUserEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    courseExecutionFunctionalities.processRemoveUser(aggregateId, eventToProcess);
                }
            }
        }
    }
}
