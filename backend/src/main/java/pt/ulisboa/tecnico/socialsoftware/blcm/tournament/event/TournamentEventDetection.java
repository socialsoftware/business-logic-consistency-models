package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.dto.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.repository.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TournamentEventDetection {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentFunctionalities tournamentFunctionalities;

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
    public void detectAnonymizeStudentEvents() {
        //System.out.println("Anonymize Detection");
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : tournamentAggregateIds) {
            Optional<Tournament> tournamentOp = tournamentRepository.findLastTournamentVersion(aggregateId);
            if (tournamentOp.isEmpty()) {
                continue;
            }
            Tournament tournament = tournamentOp.get();
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), AnonymizeExecutionStudentEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());

                for (Event eventToProcess : eventsToProcess) {
                    tournamentFunctionalities.processAnonymizeStudentEvent(aggregateId, eventToProcess);
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
    public void detectRemoveCourseExecutionEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : tournamentAggregateIds) {
            Optional<Tournament> tournamentOp = tournamentRepository.findLastTournamentVersion(aggregateId);
            if (tournamentOp.isEmpty()) {
                continue;
            }
            Tournament tournament = tournamentOp.get();
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), RemoveCourseExecutionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    tournamentFunctionalities.processRemoveCourseExecution(aggregateId, eventToProcess);
                }
            }
        }
    }

    /*
        TOPIC_EXISTS
            t: this.tournamentTopics | t.state != INACTIVE => EXISTS Topic(t.id) && t.name == Topic(t.id).name
    */
    @Scheduled(fixedDelay = 1000)
    public void detectUpdateTopicEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : tournamentAggregateIds) {
            Tournament tournament = tournamentRepository.findLastTournamentVersion(aggregateId).orElse(null);
            if (tournament == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), UpdateTopicEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    tournamentFunctionalities.processUpdateTopic(aggregateId, eventToProcess);
                }
            }
        }
    }



    /*
        TOPIC_EXISTS
            t: this.tournamentTopics | t.state != INACTIVE => EXISTS Topic(t.id) && t.name == Topic(t.id).name
    */
    @Scheduled(fixedDelay = 1000)
    public void detectDeleteTopicEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : tournamentAggregateIds) {
            Tournament tournament = tournamentRepository.findLastTournamentVersion(aggregateId).orElse(null);
            if (tournament == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), DeleteTopicEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    tournamentFunctionalities.processDeleteTopic(aggregateId, eventToProcess);
                }
            }
        }
    }

    /*
        QUIZ_ANSWER_EXISTS
            p: this.participants | (!p.answer.isEmpty && p.answer.state != INACTIVE) => EXISTS QuizAnswer(p.answer.id)
     */
    @Scheduled(fixedDelay = 1000)
    public void detectAnswerQuestionEvent() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : tournamentAggregateIds) {
            Tournament tournament = tournamentRepository.findLastTournamentVersion(aggregateId).orElse(null);
            if (tournament == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), AnswerQuestionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    tournamentFunctionalities.processAnswerQuestion(aggregateId, eventToProcess);
                }
            }
        }
    }

    /*
        CREATOR_COURSE_EXECUTION
        PARTICIPANT_COURSE_EXECUTION
     */
    
    @Scheduled(fixedDelay = 1000)
    public void detectUnenrollStudentFromCourseExecutionEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : tournamentAggregateIds) {
            Tournament tournament = tournamentRepository.findLastTournamentVersion(aggregateId).orElse(null);
            if (tournament == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), UnerollStudentFromCourseExecutionEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    tournamentFunctionalities.processUnenrollStudent(aggregateId, eventToProcess);
                }
            }
        }
    }

    /*
        QUIZ_EXISTS
            this.tournamentQuiz.state != INACTIVE => EXISTS Quiz(this.tournamentQuiz.id)
    */
    @Scheduled(fixedDelay = 1000)
    public void detectInvalidateQuizEvent() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : tournamentAggregateIds) {
            Tournament tournament = tournamentRepository.findLastTournamentVersion(aggregateId).orElse(null);
            if (tournament == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), InvalidateQuizEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    tournamentFunctionalities.processInvalidateQuizEvent(aggregateId, eventToProcess);
                }
            }
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void detectUpdateExecutionStudentNameEvent() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        for (Integer aggregateId : tournamentAggregateIds) {
            Tournament tournament = tournamentRepository.findLastTournamentVersion(aggregateId).orElse(null);
            if (tournament == null) {
                continue;
            }
            Set<EventSubscription> eventSubscriptions = tournamentService.getEventSubscriptions(tournament.getAggregateId(), tournament.getVersion(), UpdateExecutionStudentNameEvent.class.getSimpleName());
            for (EventSubscription eventSubscription : eventSubscriptions) {
                List<Event> eventsToProcess = eventRepository.findAll().stream()
                        .filter(eventSubscription::subscribesEvent)
                        .sorted(Comparator.comparing(Event::getTs).reversed())
                        .collect(Collectors.toList());
                for (Event eventToProcess : eventsToProcess) {
                    tournamentFunctionalities.processUpdateExecutionStudentNameEvent(aggregateId, eventToProcess);
                }
            }
        }
    }
}