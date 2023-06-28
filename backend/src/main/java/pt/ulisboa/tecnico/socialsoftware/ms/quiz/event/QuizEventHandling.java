package pt.ulisboa.tecnico.socialsoftware.ms.quiz.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.question.event.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.question.event.publish.UpdateQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.ms.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quiz.service.QuizService;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QuizEventHandling {
    @Autowired
    private QuizEventProcessing quizEventProcessing;
    @Autowired
    private QuizService quizService;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private EventRepository eventRepository;

    /*
        COURSE_EXECUTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleRemoveCourseExecutionEvents() {
        Set<Integer> aggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Quiz quiz = quizRepository.findLastVersion(aggregateId).orElse(null);
            if (quiz == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quizService.getEventSubscriptions(quiz.getAggregateId(), quiz.getVersion(), RemoveCourseExecutionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<RemoveCourseExecutionEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(RemoveCourseExecutionEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (RemoveCourseExecutionEvent eventToProcess : eventsToProcess) {
                    quizEventProcessing.processRemoveCourseExecutionEvent(aggregateId, eventToProcess);
                }
            }
        }
    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleUpdateQuestionEvent() {
        Set<Integer> aggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Quiz quiz = quizRepository.findLastVersion(aggregateId).orElse(null);
            if (quiz == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quizService.getEventSubscriptions(quiz.getAggregateId(), quiz.getVersion(), UpdateQuestionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<UpdateQuestionEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(UpdateQuestionEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (UpdateQuestionEvent eventToProcess : eventsToProcess) {
                    quizEventProcessing.processUpdateQuestionEvent(aggregateId, eventToProcess);
                }
            }
        }
    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleRemoveQuestionEvent() {
        Set<Integer> aggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Quiz quiz = quizRepository.findLastVersion(aggregateId).orElse(null);
            if (quiz == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quizService.getEventSubscriptions(quiz.getAggregateId(), quiz.getVersion(), RemoveQuestionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<RemoveQuestionEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(RemoveQuestionEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (RemoveQuestionEvent eventToProcess : eventsToProcess) {
                    quizEventProcessing.processRemoveQuizQuestionEvent(aggregateId, eventToProcess);
                }
            }
        }
    }
}
