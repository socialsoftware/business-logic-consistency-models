package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.quiz.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventService;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.quiz.aggregate.Quiz;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.quiz.aggregate.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.events.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.question.events.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.question.events.publish.UpdateQuestionEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QuizEventHandling {
    @Autowired
    private EventService eventService;
    @Autowired
    private QuizEventProcessing quizEventProcessing;
    @Autowired
    private QuizRepository quizRepository;

    /*
        COURSE_EXECUTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleRemoveCourseExecutionEvents() throws Throwable {
        handleQuizSubscribedEvent(RemoveCourseExecutionEvent.class, "processRemoveCourseExecutionEvent");
    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleUpdateQuestionEvent() throws Throwable {
        handleQuizSubscribedEvent(UpdateQuestionEvent.class, "processUpdateQuestionEvent");
    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleRemoveQuestionEvent() throws Throwable {
        handleQuizSubscribedEvent(RemoveQuestionEvent.class, "processRemoveQuizQuestionEvent");
    }

    private void handleQuizSubscribedEvent(Class<? extends Event> eventClass, String method) throws Throwable {
        Set<Integer> quizAggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : quizAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, eventClass);

            for (EventSubscription eventSubscription: eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, eventClass);
                for (Event eventToProcess : eventsToProcess) {
                    try {
                        quizEventProcessing.getClass().getMethod(method, Integer.class, eventClass).invoke(quizEventProcessing, subscriberAggregateId, eventToProcess);
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
