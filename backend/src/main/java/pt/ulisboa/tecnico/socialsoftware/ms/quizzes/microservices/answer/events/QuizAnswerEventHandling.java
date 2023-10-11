package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.answer.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.answer.aggregate.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.answer.aggregate.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.events.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.question.events.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.user.events.publish.RemoveUserEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.events.publish.DisenrollStudentFromCourseExecutionEvent;

import java.lang.reflect.InvocationTargetException;
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
    public void handleRemoveUserEvents() throws Throwable {
        handleQuizAnswerSubscribedEvent(RemoveUserEvent.class, "processRemoveUserEvent");
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
    public void handleRemoveQuestionEvent() throws Throwable {
        handleQuizAnswerSubscribedEvent(RemoveQuestionEvent.class, "processRemoveQuestionEvent");
    }

    /*
    QUIZ_COURSE_EXECUTION_SAME_AS_USER
     */

    @Scheduled(fixedDelay = 1000)
    public void handleUnenrollStudentEvent() throws Throwable {
        handleQuizAnswerSubscribedEvent(DisenrollStudentFromCourseExecutionEvent.class, "processUnenrollStudentEvent");
    }

    @Scheduled(fixedDelay = 1000)
    public void handleUpdateExecutionStudentNameEvent() throws Throwable {
        handleQuizAnswerSubscribedEvent(UpdateStudentNameEvent.class, "processUpdateExecutionStudentNameEvent");
    }

    private void handleQuizAnswerSubscribedEvent(Class<? extends Event> eventClass, String method) throws Throwable {
        Set<Integer> questionAnswerAggregateIds = quizAnswerRepository.findAll().stream().map(QuizAnswer::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : questionAnswerAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, eventClass);

            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, eventClass);
                for (Event eventToProcess : eventsToProcess) {
                    try {
                        quizAnswerEventProcessing.getClass().getMethod(method, Integer.class, eventClass).invoke(quizAnswerEventProcessing, subscriberAggregateId, eventToProcess);
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

