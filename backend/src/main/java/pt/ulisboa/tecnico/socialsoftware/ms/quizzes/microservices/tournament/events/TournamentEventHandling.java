package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.tournament.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.answer.events.publish.QuizAnswerQuestionAnswerEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.tournament.aggregate.Tournament;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.tournament.aggregate.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.events.publish.AnonymizeStudentEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.events.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.events.publish.DisenrollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.events.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.quiz.events.publish.InvalidateQuizEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.events.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.events.publish.UpdateTopicEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TournamentEventHandling {
    @Autowired
    private EventService eventService;
    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private TournamentEventProcessing tournamentEventProcessing;

    /* fixed delay guarantees this task only runs 10 seconds after the previous finished. With fixed delay concurrent executions are not possible.*/
    /*
    CREATOR_EXISTS
		this.creator.state != INACTIVE => EXISTS User(this.creator.id) && this.creator.username == User(this.creator.id).username && this.creator.name == User(this.creator.id).name
    PARTICIPANT_EXISTS
		Rule:
			forall p : this.tournamentParticipants | p.state != INACTIVE => EXISTS User(p.id) && p.username == User(p.id).username && p.name == User(p.id).name
		Events Subscribed:
			anonymizeUser(user: User) {
				p in this.participants | p.id == user.id
					p.username = user.username
					p.name = user.name
					p.state = INACTIVE
	*/

    @Scheduled(fixedDelay = 1000)
    public void handleAnonymizeStudentEvents() throws Throwable {
        handleTournamentSubscribedEvent(AnonymizeStudentEvent.class, "processAnonymizeStudentEvent");
    }

    /*
    COURSE_EXECUTION_EXISTS
		this.tournamentCourseExecution.state != INACTIVE => EXISTS CourseExecution(this.tournamentCourseExecution.id) // change do DELETED???
		&& this.courseExecution.courseId == CourseExecution(this.courseExecution.id).Course.id && this.courseExecution.status == CourseExecution(this.courseExecution.id).status
		&& this.courseExecution.acronym == CourseExecution(this.courseExecution.id).acronym
     */

    @Scheduled(fixedDelay = 1000)
    public void handleRemoveCourseExecutionEvents() throws Throwable {
        handleTournamentSubscribedEvent(RemoveCourseExecutionEvent.class, "processRemoveCourseExecutionEvent");
    }

    /*
        TOPIC_EXISTS
            t: this.tournamentTopics | t.state != INACTIVE => EXISTS Topic(t.id) && t.name == Topic(t.id).name
    */
    @Scheduled(fixedDelay = 1000)
    public void handleUpdateTopicEvents() throws Throwable {
        handleTournamentSubscribedEvent(UpdateTopicEvent.class, "processUpdateTopicEvent");
    }

    /*
        TOPIC_EXISTS
            t: this.tournamentTopics | t.state != INACTIVE => EXISTS Topic(t.id) && t.name == Topic(t.id).name
    */
    @Scheduled(fixedDelay = 1000)
    public void handleDeleteTopicEvents() throws Throwable {
        handleTournamentSubscribedEvent(DeleteTopicEvent.class, "processDeleteTopicEvent");
    }

    /*
        QUIZ_ANSWER_EXISTS
            p: this.participants | (!p.answer.isEmpty && p.answer.state != INACTIVE) => EXISTS QuizAnswer(p.answer.id)
     */
    @Scheduled(fixedDelay = 1000)
    public void handleAnswerQuestionEvent() throws Throwable {
        handleTournamentSubscribedEvent(QuizAnswerQuestionAnswerEvent.class, "processAnswerQuestionEvent");
    }

    /*
        CREATOR_COURSE_EXECUTION
        PARTICIPANT_COURSE_EXECUTION
     */
    
    @Scheduled(fixedDelay = 1000)
    public void handleUnenrollStudentFromCourseExecutionEvents() throws Throwable {
        handleTournamentSubscribedEvent(DisenrollStudentFromCourseExecutionEvent.class, "processUnenrollStudentEvent");
    }

    /*
        QUIZ_EXISTS
            this.tournamentQuiz.state != INACTIVE => EXISTS Quiz(this.tournamentQuiz.id)
    */
    @Scheduled(fixedDelay = 1000)
    public void handleInvalidateQuizEvent() throws Throwable {
        handleTournamentSubscribedEvent(InvalidateQuizEvent.class, "processInvalidateQuizEvent");
    }

    @Scheduled(fixedDelay = 1000)
    public void handleUpdateExecutionStudentNameEvent() throws Throwable {
        handleTournamentSubscribedEvent(UpdateStudentNameEvent.class, "processUpdateExecutionStudentNameEvent");
    }

    private void handleTournamentSubscribedEvent(Class<? extends Event> eventClass, String method) throws Throwable {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : tournamentAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, eventClass);

            for (EventSubscription eventSubscription: eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, eventClass);
                for (Event eventToProcess : eventsToProcess) {
                    try {
                        tournamentEventProcessing.getClass().getMethod(method, Integer.class, eventClass).invoke(tournamentEventProcessing, subscriberAggregateId, eventToProcess);
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