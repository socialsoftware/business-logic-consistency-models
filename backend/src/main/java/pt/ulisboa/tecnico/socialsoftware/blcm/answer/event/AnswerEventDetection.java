package pt.ulisboa.tecnico.socialsoftware.blcm.answer.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.AnswerFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.Answer;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.repository.AnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.service.AnswerService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEventsRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.*;

@Component
public class AnswerEventDetection {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProcessedEventsRepository processedEventsRepository;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private AnswerService answerService;

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
            Set<EventSubscription> eventSubscriptions = answer.getEventSubscriptionsByEventType(REMOVE_USER);
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findByIdVersionType(eventSubscription.getSenderAggregateId(), eventSubscription.getSenderLastVersion(), eventSubscription.getEventType());
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
            Set<EventSubscription> eventSubscriptions = answer.getEventSubscriptionsByEventType(REMOVE_QUESTION);
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findByIdVersionType(eventSubscription.getSenderAggregateId(), eventSubscription.getSenderLastVersion(), eventSubscription.getEventType());
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
            Set<EventSubscription> eventSubscriptions = answer.getEventSubscriptionsByEventType(UNENROLL_STUDENT);
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findByIdVersionType(eventSubscription.getSenderAggregateId(), eventSubscription.getSenderLastVersion(), eventSubscription.getEventType());
                for (Event eventToProcess : eventsToProcess) {
                    answerFunctionalities.processUnenrollStudent(aggregateId, eventToProcess);
                }
            }
        }
    }
}
