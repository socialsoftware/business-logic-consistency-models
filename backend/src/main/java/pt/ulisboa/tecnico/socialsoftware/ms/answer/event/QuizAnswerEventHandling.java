package pt.ulisboa.tecnico.socialsoftware.ms.answer.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.ms.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.question.event.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.user.event.publish.RemoveUserEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.UnerollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QuizAnswerEventHandling {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private QuizAnswerEventProcessing quizAnswerEventProcessing;
    /*
        QUIZ_COURSE_EXECUTION_SAME_AS_QUESTION_COURSE
     */

    /** NO EVENT FOR THESE neither question course or quiz course execution changed**/

    /*
        USER_EXISTS
	*/

    /*
        USER_EXISTS
	*/

    // TODO: no event associated
    @Scheduled(fixedDelay = 1000)
    public void handleRemoveUserEvents() {
        Set<Integer> aggregateIds = quizAnswerRepository.findAll().stream().map(QuizAnswer::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            QuizAnswer quizAnswer = quizAnswerRepository.findLastAnswerVersion(aggregateId).orElse(null);
            if (quizAnswer == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quizAnswer.getEventSubscriptionsByEventType(RemoveUserEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<RemoveUserEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(RemoveUserEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (RemoveUserEvent eventToProcess : eventsToProcess) {
                    quizAnswerEventProcessing.processRemoveUserEvent(aggregateId, eventToProcess);
                }
            }
        }
    }

    /*
        QUIZ_EXISTS
            Wait fo the quiz to emit events
     */

    /*
        QUESTION_EXISTS
            Should we process this??? The answer only has the ids
     */

    // TODO: no event associated
    @Scheduled(fixedDelay = 1000)
    public void handleRemoveQuestionEvent() {
        Set<Integer> aggregateIds = quizAnswerRepository.findAll().stream().map(QuizAnswer::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            QuizAnswer quizAnswer = quizAnswerRepository.findLastAnswerVersion(aggregateId).orElse(null);
            if (quizAnswer == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quizAnswer.getEventSubscriptionsByEventType(RemoveQuestionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<RemoveQuestionEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(RemoveQuestionEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (RemoveQuestionEvent eventToProcess : eventsToProcess) {
                    quizAnswerEventProcessing.processRemoveQuestionEvent(aggregateId, eventToProcess);
                }
            }
        }
    }

    /*
    QUIZ_COURSE_EXECUTION_SAME_AS_USER
     */

    @Scheduled(fixedDelay = 1000)
    public void handleUnenrollStudentEvent() {
        Set<Integer> aggregateIds = quizAnswerRepository.findAll().stream().map(QuizAnswer::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            QuizAnswer quizAnswer = quizAnswerRepository.findLastAnswerVersion(aggregateId).orElse(null);
            if (quizAnswer == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quizAnswer.getEventSubscriptionsByEventType(UnerollStudentFromCourseExecutionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<UnerollStudentFromCourseExecutionEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(UnerollStudentFromCourseExecutionEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (UnerollStudentFromCourseExecutionEvent eventToProcess : eventsToProcess) {
                    quizAnswerEventProcessing.processUnenrollStudentEvent(aggregateId, eventToProcess);
                }
            }
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void handleUpdateExecutionStudentNameEvent() {
        Set<Integer> answerAggregateIds = quizAnswerRepository.findAll().stream().map(QuizAnswer::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : answerAggregateIds) {
            QuizAnswer quizAnswer = quizAnswerRepository.findLastAnswerVersion(subscriberAggregateId).orElse(null);
            if (quizAnswer == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = quizAnswer.getEventSubscriptionsByEventType(UpdateStudentNameEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<UpdateStudentNameEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(UpdateStudentNameEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (UpdateStudentNameEvent eventToProcess : eventsToProcess) {
                    quizAnswerEventProcessing.processUpdateExecutionStudentNameEvent(quizAnswer.getAggregateId(), eventToProcess);
                }
            }
        }
    }
}
