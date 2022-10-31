package pt.ulisboa.tecnico.socialsoftware.blcm.question.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.QuestionFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.repository.QuestionRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.DELETE_TOPIC;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.UPDATE_TOPIC;

@Component
public class QuestionEventDetection {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private QuestionFunctionalities questionFunctionalities;

    @Autowired
    private QuestionRepository questionRepository;

    /*
        TOPICS_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectUpdateTopicEvents() {
        //System.out.println("Update Topic Detection");
        Set<Integer> aggregateIds = questionRepository.findAll().stream().map(Question::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Question question = questionRepository.findLastQuestionVersion(aggregateId).orElse(null);
            if (question == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = question.getEventSubscriptionsByEventType(UPDATE_TOPIC);
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findByIdVersionType(eventSubscription.getSenderAggregateId(), eventSubscription.getSenderLastVersion(), eventSubscription.getEventType());
                for (Event eventToProcess : eventsToProcess) {
                    questionFunctionalities.processUpdateTopic(aggregateId, eventToProcess);
                }
            }
        }
    }



    /*
        TOPICS_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectDeleteTopicEvents() {
        Set<Integer> aggregateIds = questionRepository.findAll().stream().map(Question::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Question question = questionRepository.findLastQuestionVersion(aggregateId).orElse(null);
            if (question == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = question.getEventSubscriptionsByEventType(DELETE_TOPIC);
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findByIdVersionType(eventSubscription.getSenderAggregateId(), eventSubscription.getSenderLastVersion(), eventSubscription.getEventType());
                for (Event e : eventsToProcess) {
                    questionFunctionalities.processRemoveTopic(aggregateId, e);
                }
            }
        }
    }



    /*
        COURSE_EXISTS
     */
}
