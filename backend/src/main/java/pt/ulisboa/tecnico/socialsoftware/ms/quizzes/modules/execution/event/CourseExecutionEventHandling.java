package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.execution.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventService;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.user.event.publish.RemoveUserEvent;

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
