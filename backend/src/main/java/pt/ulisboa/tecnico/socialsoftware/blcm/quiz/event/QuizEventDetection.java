package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.dto.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.UpdateQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.repository.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QuizEventDetection {
    @Autowired
    private QuizFunctionalities quizFunctionalities;

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
    public void detectRemoveCourseExecutionEvents() {
        Set<Integer> aggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Quiz quiz = quizRepository.findLastVersion(aggregateId).orElse(null);
            if (quiz == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quizService.getEventSubscriptions(quiz.getAggregateId(), quiz.getVersion(), RemoveCourseExecutionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    quizFunctionalities.processRemoveCourseExecutionEvent(aggregateId, eventToProcess);
                }
            }
        }
    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectUpdateQuestionEvent() {
        Set<Integer> aggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Quiz quiz = quizRepository.findLastVersion(aggregateId).orElse(null);
            if (quiz == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quizService.getEventSubscriptions(quiz.getAggregateId(), quiz.getVersion(), UpdateQuestionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    quizFunctionalities.processUpdateQuestion(aggregateId, eventToProcess);
                }
            }
        }
    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectRemoveQuestionEvent() {
        Set<Integer> aggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Quiz quiz = quizRepository.findLastVersion(aggregateId).orElse(null);
            if (quiz == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quizService.getEventSubscriptions(quiz.getAggregateId(), quiz.getVersion(), RemoveQuestionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    quizFunctionalities.processRemoveQuestion(aggregateId, eventToProcess);
                }
            }
        }
    }
}
