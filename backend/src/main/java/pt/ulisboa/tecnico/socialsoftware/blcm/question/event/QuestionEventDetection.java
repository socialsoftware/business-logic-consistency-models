package pt.ulisboa.tecnico.socialsoftware.blcm.question.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEvents;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEventsRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.DELETE_TOPIC;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.UPDATE_TOPIC;

public class QuestionEventDetection {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProcessedEventsRepository processedEventsRepository;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

    /*
        TOPICS_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectUpdateTopicEvents() {
        Set<Integer> questionAggregateIds = questionRepository.findAll().stream().map(Question::getAggregateId).collect(Collectors.toSet());
        List<Event> events = getEmittedEvents(UPDATE_TOPIC);
        for(Integer questionAggregateId : questionAggregateIds) {
            ProcessedEvents processedEvents = getTournamentProcessedEvents(UPDATE_TOPIC, questionAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processUpdateTopicEvents(questionAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processUpdateTopicEvents(Integer questionAggregateId, List<Event> events) {
        Set<java.lang.Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<UpdateTopicEvent> updatedTopicEvents = events.stream()
                .map(e -> UpdateTopicEvent.class.cast(e))
                .collect(Collectors.toSet());
        for(UpdateTopicEvent e : updatedTopicEvents) {
            Set<java.lang.Integer> tournamentIdsByTopic = questionRepository.findAllAggregateIdsByTopic(e.getAggregateId());
            if(!tournamentIdsByTopic.contains(questionAggregateId)) {
                continue;
            }
            System.out.printf("Processing update topic %d event for question %d\n", e.getAggregateId(), questionAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Question updatedQuestion = questionService.updateTopic(questionAggregateId, e.getAggregateId(), e.getTopicName() ,e.getAggregateVersion(), unitOfWork);
            if(updatedQuestion != null) {
                updatedQuestion.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
    }

    /*
        TOPICS_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectDeleteTopicEvents() {
        Set<Integer> questionAggregateIds = questionRepository.findAll().stream().map(Question::getAggregateId).collect(Collectors.toSet());
        List<Event> events = getEmittedEvents(DELETE_TOPIC);
        for(Integer questionAggregateId : questionAggregateIds) {
            ProcessedEvents processedEvents = getTournamentProcessedEvents(DELETE_TOPIC, questionAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processDeleteTopicEvents(questionAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processDeleteTopicEvents(Integer questionAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<DeleteTopicEvent> deleteTopicEvents = events.stream()
                .map(e -> DeleteTopicEvent.class.cast(e))
                .collect(Collectors.toSet());
        for(DeleteTopicEvent e : deleteTopicEvents) {
            Set<Integer> tournamentIdsByTopic = questionRepository.findAllAggregateIdsByTopic(e.getAggregateId());
            if(!tournamentIdsByTopic.contains(questionAggregateId)) {
                continue;
            }
            System.out.printf("Processing remove topic %d event for question %d\n", e.getAggregateId(), questionAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Question updatedQuestion = questionService.removeTopic(questionAggregateId, e.getAggregateId(), e.getAggregateVersion(), unitOfWork);
            if(updatedQuestion != null) {
                updatedQuestion.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
    }

    private List<Event> getEmittedEvents(String eventType) {
        return eventRepository.findAll()
                .stream()
                .filter(e -> eventType.equals(e.getType()))
                .distinct()
                .sorted(Comparator.comparing(Event::getTs).reversed())
                .collect(Collectors.toList());
    }

    private ProcessedEvents getTournamentProcessedEvents(String eventType, Integer aggregateId) {
        return processedEventsRepository.findAll().stream()
                .filter(pe -> aggregateId.equals(pe.getAggregateId()))
                .filter(pe -> eventType.equals(pe.getEventType()))
                .findFirst()
                .orElse(new ProcessedEvents(eventType, aggregateId));
    }

    /*
        COURSE_EXISTS
     */
}
