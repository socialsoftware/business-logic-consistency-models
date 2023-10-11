package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventService;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.aggregate.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.aggregate.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.user.events.publish.RemoveUserEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CourseExecutionEventHandling {
    @Autowired
    private EventService eventService;
    @Autowired
    private CourseExecutionEventProcessing courseExecutionEventProcessing;
    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    /*
        USER_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleRemoveUserEvents() {
        Set<Integer> courseExecutionAggregateIds = courseExecutionRepository.findAll().stream().map(CourseExecution::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : courseExecutionAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, RemoveUserEvent.class);

            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, RemoveUserEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    courseExecutionEventProcessing.processRemoveUserEvent(subscriberAggregateId, (RemoveUserEvent) eventToProcess);
                }
            }
        }
    }
}
