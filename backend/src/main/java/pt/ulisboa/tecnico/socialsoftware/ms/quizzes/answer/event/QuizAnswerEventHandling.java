package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.event.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.event.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.event.publish.RemoveUserEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.event.publish.UnerollStudentFromCourseExecutionEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QuizAnswerEventHandling {
    @Autowired
    private EventService eventService;
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
        Set<Integer> questionAnswerAggregateIds = quizAnswerRepository.findAll().stream().map(QuizAnswer::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : questionAnswerAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, RemoveUserEvent.class);

            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, RemoveUserEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    quizAnswerEventProcessing.processRemoveUserEvent(subscriberAggregateId, (RemoveUserEvent) eventToProcess);
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
        Set<Integer> questionAnswerAggregateIds = quizAnswerRepository.findAll().stream().map(QuizAnswer::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : questionAnswerAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, RemoveQuestionEvent.class);

            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, RemoveQuestionEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    quizAnswerEventProcessing.processRemoveQuestionEvent(subscriberAggregateId, (RemoveQuestionEvent) eventToProcess);
                }
            }
        }
    }

    /*
    QUIZ_COURSE_EXECUTION_SAME_AS_USER
     */

    @Scheduled(fixedDelay = 1000)
    public void handleUnenrollStudentEvent() {
        Set<Integer> questionAnswerAggregateIds = quizAnswerRepository.findAll().stream().map(QuizAnswer::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : questionAnswerAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, UnerollStudentFromCourseExecutionEvent.class);

            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, UnerollStudentFromCourseExecutionEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    quizAnswerEventProcessing.processUnenrollStudentEvent(subscriberAggregateId, (UnerollStudentFromCourseExecutionEvent) eventToProcess);
                }
            }
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void handleUpdateExecutionStudentNameEvent() {
        Set<Integer> questionAnswerAggregateIds = quizAnswerRepository.findAll().stream().map(QuizAnswer::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : questionAnswerAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, UpdateStudentNameEvent.class);

            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, UpdateStudentNameEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    quizAnswerEventProcessing.processUpdateExecutionStudentNameEvent(subscriberAggregateId, (UpdateStudentNameEvent) eventToProcess);
                }
            }
        }
    }
}

