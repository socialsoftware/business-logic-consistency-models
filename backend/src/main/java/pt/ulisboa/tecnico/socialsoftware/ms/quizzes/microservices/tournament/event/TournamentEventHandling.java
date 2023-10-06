package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.tournament.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.answer.event.publish.QuizAnswerQuestionAnswerEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.tournament.aggregate.Tournament;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.tournament.aggregate.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.event.publish.AnonymizeStudentEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.event.publish.DisenrollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.execution.event.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.quiz.event.publish.InvalidateQuizEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.topic.event.publish.UpdateTopicEvent;

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
    public void handleAnonymizeStudentEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : tournamentAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, AnonymizeStudentEvent.class);

            for (EventSubscription eventSubscription: eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, AnonymizeStudentEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processAnonymizeStudentEvent(subscriberAggregateId, (AnonymizeStudentEvent) eventToProcess);
                }
            }
        }
    }

    /*
    COURSE_EXECUTION_EXISTS
		this.tournamentCourseExecution.state != INACTIVE => EXISTS CourseExecution(this.tournamentCourseExecution.id) // change do DELETED???
		&& this.courseExecution.courseId == CourseExecution(this.courseExecution.id).Course.id && this.courseExecution.status == CourseExecution(this.courseExecution.id).status
		&& this.courseExecution.acronym == CourseExecution(this.courseExecution.id).acronym
     */

    @Scheduled(fixedDelay = 1000)
    public void handleRemoveCourseExecutionEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : tournamentAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, RemoveCourseExecutionEvent.class);

            for (EventSubscription eventSubscription: eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, RemoveCourseExecutionEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processRemoveCourseExecutionEvent(subscriberAggregateId, (RemoveCourseExecutionEvent) eventToProcess);
                }
            }
        }
    }

    /*
        TOPIC_EXISTS
            t: this.tournamentTopics | t.state != INACTIVE => EXISTS Topic(t.id) && t.name == Topic(t.id).name
    */
    @Scheduled(fixedDelay = 1000)
    public void handleUpdateTopicEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : tournamentAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, UpdateTopicEvent.class);

            for (EventSubscription eventSubscription: eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, UpdateTopicEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processUpdateTopicEvent(subscriberAggregateId, (UpdateTopicEvent) eventToProcess);
                }
            }
        }
    }

    /*
        TOPIC_EXISTS
            t: this.tournamentTopics | t.state != INACTIVE => EXISTS Topic(t.id) && t.name == Topic(t.id).name
    */
    @Scheduled(fixedDelay = 1000)
    public void handleDeleteTopicEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : tournamentAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, DeleteTopicEvent.class);

            for (EventSubscription eventSubscription: eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, DeleteTopicEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processDeleteTopicEvent(subscriberAggregateId, (DeleteTopicEvent) eventToProcess);
                }
            }
        }
    }

    /*
        QUIZ_ANSWER_EXISTS
            p: this.participants | (!p.answer.isEmpty && p.answer.state != INACTIVE) => EXISTS QuizAnswer(p.answer.id)
     */
    @Scheduled(fixedDelay = 1000)
    public void handleAnswerQuestionEvent() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : tournamentAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, QuizAnswerQuestionAnswerEvent.class);

            for (EventSubscription eventSubscription: eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, QuizAnswerQuestionAnswerEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processAnswerQuestionEvent(subscriberAggregateId, (QuizAnswerQuestionAnswerEvent) eventToProcess);
                }
            }
        }
    }

    /*
        CREATOR_COURSE_EXECUTION
        PARTICIPANT_COURSE_EXECUTION
     */
    
    @Scheduled(fixedDelay = 1000)
    public void handleUnenrollStudentFromCourseExecutionEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : tournamentAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, DisenrollStudentFromCourseExecutionEvent.class);

            for (EventSubscription eventSubscription: eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, DisenrollStudentFromCourseExecutionEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processUnenrollStudentEvent(subscriberAggregateId, (DisenrollStudentFromCourseExecutionEvent) eventToProcess);
                }
            }
        }
    }

    /*
        QUIZ_EXISTS
            this.tournamentQuiz.state != INACTIVE => EXISTS Quiz(this.tournamentQuiz.id)
    */
    @Scheduled(fixedDelay = 1000)
    public void handleInvalidateQuizEvent() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : tournamentAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, InvalidateQuizEvent.class);

            for (EventSubscription eventSubscription: eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, InvalidateQuizEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processInvalidateQuizEvent(subscriberAggregateId, (InvalidateQuizEvent) eventToProcess);
                }
            }
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void handleUpdateExecutionStudentNameEvent() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : tournamentAggregateIds) {
            Set<EventSubscription> eventSubscriptions = eventService.getEventSubscriptions(subscriberAggregateId, UpdateStudentNameEvent.class);

            for (EventSubscription eventSubscription: eventSubscriptions) {
                List<? extends Event> eventsToProcess = eventService.getSubscribedEvents(eventSubscription, UpdateStudentNameEvent.class);
                for (Event eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processUpdateExecutionStudentNameEvent(subscriberAggregateId, (UpdateStudentNameEvent) eventToProcess);
                }
            }
        }
    }

}