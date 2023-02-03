package pt.ulisboa.tecnico.socialsoftware.blcm.answer.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.Answer;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.AnswerFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.repository.AnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.RemoveUserEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.UnerollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.repository.EventRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AnswerEventDetection {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private AnswerFunctionalities answerFunctionalities;
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
    @Scheduled(fixedDelay = 1000)
    public void detectRemoveUserEvents() {
        Set<Integer> aggregateIds = answerRepository.findAll().stream().map(Answer::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Answer answer = answerRepository.findLastQuestionVersion(aggregateId).orElse(null);
            if (answer == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = answer.getEventSubscriptionsByEventType(RemoveUserEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    answerFunctionalities.processRemoveUser(aggregateId, eventToProcess);
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

    @Scheduled(fixedDelay = 1000)
    public void detectRemoveQuestionEvent() {
        Set<Integer> aggregateIds = answerRepository.findAll().stream().map(Answer::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Answer answer = answerRepository.findLastQuestionVersion(aggregateId).orElse(null);
            if (answer == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = answer.getEventSubscriptionsByEventType(RemoveUserEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    answerFunctionalities.processRemoveQuestion(aggregateId, eventToProcess);
                }
            }
        }
    }

    /*
    QUIZ_COURSE_EXECUTION_SAME_AS_USER
     */

    @Scheduled(fixedDelay = 1000)
    public void detectUnenrollStudentEvent() {
        Set<Integer> aggregateIds = answerRepository.findAll().stream().map(Answer::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : aggregateIds) {
            Answer answer = answerRepository.findLastQuestionVersion(aggregateId).orElse(null);
            if (answer == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = answer.getEventSubscriptionsByEventType(UnerollStudentFromCourseExecutionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    answerFunctionalities.processUnenrollStudent(aggregateId, eventToProcess);
                }
            }
        }
    }
}
