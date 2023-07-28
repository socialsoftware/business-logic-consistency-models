package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.event.publish.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QuestionEventHandling {
    @Autowired
    private EventService eventService;
    @Autowired
    private QuestionEventProcessing questionEventProcessing;
    @Autowired
    private QuestionRepository questionRepository;

    /*
        TOPICS_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleUpdateTopicEvents() {
        Set<Integer> questionAggregateIds = questionRepository.findAll().stream().map(Question::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : questionAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, UpdateTopicEvent.class);

            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, UpdateTopicEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    questionEventProcessing.processUpdateTopic(subscriberAggregateId, (UpdateTopicEvent) eventToProcess);
                }
            }
        }
    }

    /*
        TOPICS_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleDeleteTopicEvents() {
        Set<Integer> questionAggregateIds = questionRepository.findAll().stream().map(Question::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : questionAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, DeleteTopicEvent.class);

            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, DeleteTopicEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    questionEventProcessing.processRemoveTopic(subscriberAggregateId, (DeleteTopicEvent) eventToProcess);
                }
            }
        }
    }

    /*
        COURSE_EXISTS
     */
}
