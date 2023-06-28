package pt.ulisboa.tecnico.socialsoftware.ms.question.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.topic.event.publish.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.question.service.QuestionService;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QuestionEventHandling {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private QuestionEventProcessingEvent questionEventProcessingEvent;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionRepository questionRepository;

    /*
        TOPICS_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleUpdateTopicEvents() {
        //System.out.println("Update Topic Detection");
        Set<Integer> aggregateIds = questionRepository.findAll().stream().map(Question::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Question question = questionRepository.findLastVersion(aggregateId).orElse(null);
            if (question == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = questionService.getEventSubscriptions(question.getAggregateId(), question.getVersion(), UpdateTopicEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<UpdateTopicEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(UpdateTopicEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (UpdateTopicEvent eventToProcess : eventsToProcess) {
                    questionEventProcessingEvent.processUpdateTopic(aggregateId, eventToProcess);
                }
            }
        }
    }

    /*
        TOPICS_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleDeleteTopicEvents() {
        Set<Integer> aggregateIds = questionRepository.findAll().stream().map(Question::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Question question = questionRepository.findLastVersion(aggregateId).orElse(null);
            if (question == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = questionService.getEventSubscriptions(question.getAggregateId(), question.getVersion(), DeleteTopicEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<DeleteTopicEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(DeleteTopicEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (DeleteTopicEvent e : eventsToProcess) {
                    questionEventProcessingEvent.processRemoveTopic(aggregateId, e);
                }
            }
        }
    }


    /*
        COURSE_EXISTS
     */
}
