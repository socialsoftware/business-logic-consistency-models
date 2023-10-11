package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.question.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.question.aggregate.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.events.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.events.publish.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.question.aggregate.Question;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;

import java.lang.reflect.InvocationTargetException;
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
    public void handleUpdateTopicEvents() throws Throwable {
        handleQuestionSubscribedEvent(UpdateTopicEvent.class, "processUpdateTopic");
    }

    /*
        TOPICS_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleDeleteTopicEvents() throws Throwable {
        handleQuestionSubscribedEvent(DeleteTopicEvent.class, "processRemoveTopic");
    }

    /*
        COURSE_EXISTS
     */

    private void handleQuestionSubscribedEvent(Class<? extends Event> eventClass, String method) throws Throwable {
        Set<Integer> questionAggregateIds = questionRepository.findAll().stream().map(Question::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : questionAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, eventClass);

            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, eventClass);
                for (Event eventToProcess : eventsToProcess) {
                    try {
                        questionEventProcessing.getClass().getMethod(method, Integer.class, eventClass).invoke(questionEventProcessing, subscriberAggregateId, eventToProcess);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    } catch (NoSuchMethodException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
