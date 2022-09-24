package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;

import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.*;

@Component
public class TournamentEventDetection {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TournamentFunctionalities tournamentFunctionalities;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private TournamentProcessedEventsRepository tournamentProcessedEventsRepository;

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

    @Scheduled(fixedDelay = 10000)
    public void detectAnonymizeUserEvents() {
        TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                .filter(e -> ANONYMIZE_USER.equals(e.getEventType()))
                .findFirst()
                .orElse(new TournamentProcessedEvents(ANONYMIZE_USER));

        Set<AnonymizeUserEvent> events = eventRepository.findAll()
                .stream()
                .filter(e -> ANONYMIZE_USER.equals(e.getType()))
                .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                .map(e -> (AnonymizeUserEvent) e)
                .collect(Collectors.toSet());

        for(AnonymizeUserEvent e : events) {
            System.out.println("Processing anonymize user events in tournament.");
            Set<Integer> tournamentsAggregateIds = tournamentRepository.findAllAggregateIdsByUser(e.getUserAggregateId());

            for (Integer tournamentId : tournamentsAggregateIds) {
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.anonymizeUser(tournamentId, e.getUserAggregateId(), e.getName(), e.getUsername(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);
            }
        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        tournamentProcessedEvents.addProcessedEventsIds(processedEventsIds);
        tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
    }

    /*
    COURSE_EXECUTION_EXISTS
		this.tournamentCourseExecution.state != INACTIVE => EXISTS CourseExecution(this.tournamentCourseExecution.id) // change do DELETED???
		&& this.courseExecution.courseId == CourseExecution(this.courseExecution.id).Course.id && this.courseExecution.status == CourseExecution(this.courseExecution.id).status
		&& this.courseExecution.acronym == CourseExecution(this.courseExecution.id).acronym
     */

    @Scheduled(fixedDelay = 10000)
    public void detectRemoveCourseExecutionEvents() {
        TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                .filter(e -> REMOVE_COURSE_EXECUTION.equals(e.getEventType()))
                .findFirst()
                .orElse(new TournamentProcessedEvents(REMOVE_COURSE_EXECUTION));

        Set<RemoveCourseExecutionEvent> events = eventRepository.findAll()
                .stream()
                .filter(e -> REMOVE_COURSE_EXECUTION.equals(e.getType()))
                .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                .map(e -> (RemoveCourseExecutionEvent) e)
                .collect(Collectors.toSet());



        for(RemoveCourseExecutionEvent e : events) {
            System.out.println("Processing remove course execution events in tournament.");
            Set<Integer> tournamentsAggregateIds = tournamentRepository.findAllAggregateIdsByCourseExecution(e.getCourseExecutionAggregateId());

            for (Integer tournamentAggregateId : tournamentsAggregateIds) {
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.removeCourseExecution(tournamentAggregateId, e.getCourseExecutionAggregateId(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);
            }
        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        tournamentProcessedEvents.addProcessedEventsIds(processedEventsIds);
        tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
    }

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

    @Scheduled(fixedDelay = 10000)
    public void detectRemoveUserEvents() {
        TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                .filter(e -> REMOVE_USER.equals(e.getEventType()))
                .findFirst()
                .orElse(new TournamentProcessedEvents(REMOVE_USER));

        Set<RemoveUserEvent> events = eventRepository.findAll()
                .stream()
                .filter(e -> REMOVE_USER.equals(e.getType()))
                .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                .map(e -> (RemoveUserEvent) e)
                .collect(Collectors.toSet());

        for(RemoveUserEvent e : events) {
            System.out.println("Processing remove user events in tournament.");

            Set<Integer> tournamentsAggregateIds = tournamentRepository.findAllAggregateIdsByUser(e.getUserAggregateId());

            tournamentsAggregateIds.forEach(aggregateId -> {
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.removeUser(aggregateId, e.getUserAggregateId(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);
            });
        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        tournamentProcessedEvents.addProcessedEventsIds(processedEventsIds);
        tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
    }


    /*
        TOPIC_EXISTS
            t: this.tournamentTopics | t.state != INACTIVE => EXISTS Topic(t.id) && t.name == Topic(t.id).name
    */
    @Scheduled(fixedDelay = 10000)
    public void detectUpdateTopicEvents() {
        TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                .filter(e -> UPDATE_TOPIC.equals(e.getEventType()))
                .findFirst()
                .orElse(new TournamentProcessedEvents(UPDATE_TOPIC));

        Set<UpdateTopicEvent> events = eventRepository.findAll()
                .stream()
                .filter(e -> UPDATE_TOPIC.equals(e.getType()))
                .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                .map(e -> (UpdateTopicEvent) e)
                .collect(Collectors.toSet());

        for(UpdateTopicEvent e : events) {
            System.out.println("Processing update topics events in tournament.");
            Set<Integer> tournamentsAggregateIds = tournamentRepository.findAllAggregateIdsByTopic(e.getAggregateId());

            tournamentsAggregateIds.forEach(aggregateId -> {
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.updateTopic(aggregateId, e.getAggregateId(), e.getName(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);
            });
        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        tournamentProcessedEvents.addProcessedEventsIds(processedEventsIds);
        tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
    }

    /*
        TOPIC_EXISTS
            t: this.tournamentTopics | t.state != INACTIVE => EXISTS Topic(t.id) && t.name == Topic(t.id).name
    */
    @Scheduled(fixedDelay = 10000)
    public void detectDeleteTopicEvents() {
        TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                .filter(e -> DELETE_TOPIC.equals(e.getEventType()))
                .findFirst()
                .orElse(new TournamentProcessedEvents(DELETE_TOPIC));

        Set<DeleteTopicEvent> events = eventRepository.findAll()
                .stream()
                .filter(e -> DELETE_TOPIC.equals(e.getType()))
                .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                .map(e -> (DeleteTopicEvent) e)
                .collect(Collectors.toSet());

        for(DeleteTopicEvent e : events) {
            System.out.println("Processing delete topics events in tournament.");
            Set<Integer> tournamentsAggregateIds = tournamentRepository.findAllAggregateIdsByTopic(e.getAggregateId());

            tournamentsAggregateIds.forEach(aggregateId -> {
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.removeTopic(aggregateId, e.getAggregateId(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);
            });
        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        tournamentProcessedEvents.addProcessedEventsIds(processedEventsIds);
        tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
    }

    /*
        QUIZ_ANSWER_EXISTS
            p: this.participants | (!p.answer.isEmpty && p.answer.state != INACTIVE) => EXISTS QuizAnswer(p.answer.id)
     */
    @Scheduled(fixedDelay = 10000)
    public void detectAnswerQuestionEvent() {
        TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                .filter(e -> ANSWER_QUESTION.equals(e.getEventType()))
                .findFirst()
                .orElse(new TournamentProcessedEvents(ANSWER_QUESTION));

        Set<AnswerQuestionEvent> events = eventRepository.findAll()
                .stream()
                .filter(e -> ANSWER_QUESTION.equals(e.getType()))
                .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                .map(e -> (AnswerQuestionEvent) e)
                .collect(Collectors.toSet());

        for(AnswerQuestionEvent e : events) {
            System.out.println("Processing answer question in tournament.");
            Set<Integer> tournamentsAggregateIds = tournamentRepository.findAllAggregateIdsByQuiz(e.getQuizAggregateId());

            tournamentsAggregateIds.forEach(aggregateId -> {
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.updateParticipantAnswer(aggregateId, e.getUserAggregateId(), e.getAnswerAggregateId() ,e.isCorrect() ,unitOfWork);
                unitOfWorkService.commit(unitOfWork);
            });
        }

        Set<Integer> processedEventsIds = events.stream()
                .map(DomainEvent::getId)
                .collect(Collectors.toSet());
        tournamentProcessedEvents.addProcessedEventsIds(processedEventsIds);
        tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
    }

    /*
        QUIZ_EXISTS
            this.tournamentQuiz.state != INACTIVE => EXISTS Quiz(this.tournamentQuiz.id)
    */
}