package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.event.publish.QuizAnswerQuestionAnswerEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.AnonymizeStudentEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.UnerollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.event.publish.InvalidateQuizEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.event.publish.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TournamentEventHandling {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentFunctionalities tournamentFunctionalities;

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
        //System.out.println("Anonymize Detection");
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : tournamentAggregateIds) {
            Optional<Tournament> tournamentOp = tournamentRepository.findLastVersion(aggregateId);
            if (tournamentOp.isEmpty()) {
                continue;
            }
            Tournament tournament = tournamentOp.get();
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), AnonymizeStudentEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<AnonymizeStudentEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(AnonymizeStudentEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());

                for (AnonymizeStudentEvent eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processAnonymizeStudentEvent(aggregateId, eventToProcess);
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
        for (Integer aggregateId : tournamentAggregateIds) {
            Optional<Tournament> tournamentOp = tournamentRepository.findLastVersion(aggregateId);
            if (tournamentOp.isEmpty()) {
                continue;
            }
            Tournament tournament = tournamentOp.get();
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), RemoveCourseExecutionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<RemoveCourseExecutionEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(RemoveCourseExecutionEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (RemoveCourseExecutionEvent eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processRemoveCourseExecution(aggregateId, eventToProcess);
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
        for (Integer aggregateId : tournamentAggregateIds) {
            Tournament tournament = tournamentRepository.findLastVersion(aggregateId).orElse(null);
            if (tournament == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), UpdateTopicEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<UpdateTopicEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(UpdateTopicEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (UpdateTopicEvent eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processUpdateTopic(aggregateId, eventToProcess);
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
        for (Integer aggregateId : tournamentAggregateIds) {
            Tournament tournament = tournamentRepository.findLastVersion(aggregateId).orElse(null);
            if (tournament == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), DeleteTopicEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<DeleteTopicEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(DeleteTopicEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (DeleteTopicEvent eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processDeleteTopic(aggregateId, eventToProcess);
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
        for (Integer aggregateId : tournamentAggregateIds) {
            Tournament tournament = tournamentRepository.findLastVersion(aggregateId).orElse(null);
            if (tournament == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), QuizAnswerQuestionAnswerEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<QuizAnswerQuestionAnswerEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(QuizAnswerQuestionAnswerEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (QuizAnswerQuestionAnswerEvent eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processAnswerQuestion(aggregateId, eventToProcess);
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
        for (Integer aggregateId : tournamentAggregateIds) {
            Tournament tournament = tournamentRepository.findLastVersion(aggregateId).orElse(null);
            if (tournament == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), UnerollStudentFromCourseExecutionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<UnerollStudentFromCourseExecutionEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(UnerollStudentFromCourseExecutionEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (UnerollStudentFromCourseExecutionEvent eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processUnenrollStudent(aggregateId, eventToProcess);
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
        for (Integer aggregateId : tournamentAggregateIds) {
            Tournament tournament = tournamentRepository.findLastVersion(aggregateId).orElse(null);
            if (tournament == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), InvalidateQuizEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<InvalidateQuizEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(InvalidateQuizEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (InvalidateQuizEvent eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processInvalidateQuizEvent(aggregateId, eventToProcess);
                }
            }
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void handleUpdateExecutionStudentNameEvent() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer subscriberAggregateId : tournamentAggregateIds) {
            Tournament tournament = tournamentRepository.findLastVersion(subscriberAggregateId).orElse(null);
            if (tournament == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), UpdateStudentNameEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<UpdateStudentNameEvent> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .map(UpdateStudentNameEvent.class::cast)
                        .sorted(Comparator.comparing(Event::getTimestamp).reversed())
                        .collect(Collectors.toList());
                for (UpdateStudentNameEvent eventToProcess : eventsToProcess) {
                    tournamentEventProcessing.processUpdateExecutionStudentNameEvent(tournament.getAggregateId(), eventToProcess);
                }
            }
        }
    }
}