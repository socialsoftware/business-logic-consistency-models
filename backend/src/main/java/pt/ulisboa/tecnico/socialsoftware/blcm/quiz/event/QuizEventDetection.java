package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.*;

@Component
public class QuizEventDetection {
    @Autowired
    private QuizFunctionalities quizFunctionalities;

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
            Quiz quiz = quizRepository.findLastQuestionVersion(aggregateId).orElse(null);
            if (quiz == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quiz.getEventSubscriptionsByEventType(REMOVE_COURSE_EXECUTION);
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findByIdVersionType(eventSubscription.getSenderAggregateId(), eventSubscription.getSenderLastVersion(), eventSubscription.getEventType());
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
            Quiz quiz = quizRepository.findLastQuestionVersion(aggregateId).orElse(null);
            if (quiz == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quiz.getEventSubscriptionsByEventType(UPDATE_QUESTION);
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findByIdVersionType(eventSubscription.getSenderAggregateId(), eventSubscription.getSenderLastVersion(), eventSubscription.getEventType());
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
            Quiz quiz = quizRepository.findLastQuestionVersion(aggregateId).orElse(null);
            if (quiz == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quiz.getEventSubscriptionsByEventType(REMOVE_QUESTION);
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findByIdVersionType(eventSubscription.getSenderAggregateId(), eventSubscription.getSenderLastVersion(), eventSubscription.getEventType());
                for (Event eventToProcess : eventsToProcess) {
                    quizFunctionalities.processRemoveQuestion(aggregateId, eventToProcess);
                }
            }
        }
    }
}
