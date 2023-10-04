package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.quiz.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventService;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.question.event.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.question.event.publish.UpdateQuestionEvent;

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
    public void handleRemoveCourseExecutionEvents() {
        Set<Integer> quizAggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : quizAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, RemoveCourseExecutionEvent.class);

            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, RemoveCourseExecutionEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    quizEventProcessing.processRemoveCourseExecutionEvent(subscriberAggregateId, (RemoveCourseExecutionEvent) eventToProcess);
                }
            }
        }
    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleUpdateQuestionEvent() {
        Set<Integer> quizAggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : quizAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, UpdateQuestionEvent.class);

            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, UpdateQuestionEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    quizEventProcessing.processUpdateQuestionEvent(subscriberAggregateId, (UpdateQuestionEvent) eventToProcess);
                }
            }
        }
    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void handleRemoveQuestionEvent() {
        Set<Integer> quizAggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : quizAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, RemoveQuestionEvent.class);

            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, RemoveQuestionEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    quizEventProcessing.processRemoveQuizQuestionEvent(subscriberAggregateId, (RemoveQuestionEvent) eventToProcess);
                }
            }
        }
    }
}
