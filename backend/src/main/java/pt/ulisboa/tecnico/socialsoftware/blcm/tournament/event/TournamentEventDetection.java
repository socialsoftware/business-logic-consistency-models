package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.repository.UnitOfWorkRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
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

    @Autowired
    private UnitOfWorkRepository unitOfWorkRepository;

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
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());

        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                    .filter(pe -> tournamentAggregateId.equals(pe.getAggregateId()))
                    .filter(pe -> ANONYMIZE_USER.equals(pe.getEventType()))
                    .findFirst()
                    .orElse(new TournamentProcessedEvents(ANONYMIZE_USER, tournamentAggregateId));

            Set<AnonymizeUserEvent> events = eventRepository.findAll()
                    .stream()
                    .filter(e -> ANONYMIZE_USER.equals(e.getType()))
                    .map(e -> (AnonymizeUserEvent) e)
                    .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                    .collect(Collectors.toSet());

            for(AnonymizeUserEvent e : events) {
                Set<Integer> tournamentIdsByUser = tournamentRepository.findAllAggregateIdsByUser(e.getUserAggregateId());
                boolean runningTransactions = unitOfWorkRepository.findRunningTransactions(tournamentAggregateId, e.getAggregateVersion()).size() > 0;
                if(runningTransactions || !tournamentIdsByUser.contains(tournamentAggregateId)) {
                    continue;
                }
                System.out.printf("Processing anonymize user %d event for tournament %d\n", e.getUserAggregateId(), tournamentAggregateId);
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.anonymizeUser(tournamentAggregateId, e.getUserAggregateId(), e.getName(), e.getUsername(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);

                tournamentProcessedEvents.addProcessedEventsIds(e.getId());
            }
            tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
        }
    }

    /*
    COURSE_EXECUTION_EXISTS
		this.tournamentCourseExecution.state != INACTIVE => EXISTS CourseExecution(this.tournamentCourseExecution.id) // change do DELETED???
		&& this.courseExecution.courseId == CourseExecution(this.courseExecution.id).Course.id && this.courseExecution.status == CourseExecution(this.courseExecution.id).status
		&& this.courseExecution.acronym == CourseExecution(this.courseExecution.id).acronym
     */

    @Scheduled(fixedDelay = 10000)
    public void detectRemoveCourseExecutionEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());

        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                    .filter(pe -> tournamentAggregateId.equals(pe.getAggregateId()))
                    .filter(pe -> REMOVE_COURSE_EXECUTION.equals(pe.getEventType()))
                    .findFirst()
                    .orElse(new TournamentProcessedEvents(REMOVE_COURSE_EXECUTION, tournamentAggregateId));

            Set<RemoveCourseExecutionEvent> events = eventRepository.findAll()
                    .stream()
                    .filter(e -> REMOVE_COURSE_EXECUTION.equals(e.getType()))
                    .map(e -> (RemoveCourseExecutionEvent) e)
                    .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                    .collect(Collectors.toSet());

            for(RemoveCourseExecutionEvent e : events) {
                Set<Integer> tournamentIdsByCourseExecution = tournamentRepository.findAllAggregateIdsByCourseExecution(e.getCourseExecutionAggregateId());
                boolean runningTransactions = unitOfWorkRepository.findRunningTransactions(tournamentAggregateId, e.getAggregateVersion()).size() > 0;
                if(runningTransactions || !tournamentIdsByCourseExecution.contains(tournamentAggregateId)) {
                    continue;
                }
                System.out.printf("Processing remove course execution %d event for tournament %d\n", e.getCourseExecutionAggregateId(), tournamentAggregateId);
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.removeCourseExecution(tournamentAggregateId, e.getCourseExecutionAggregateId(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);

                tournamentProcessedEvents.addProcessedEventsIds(e.getId());
            }
            tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
        }
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
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());

        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                    .filter(pe -> tournamentAggregateId.equals(pe.getAggregateId()))
                    .filter(pe -> REMOVE_USER.equals(pe.getEventType()))
                    .findFirst()
                    .orElse(new TournamentProcessedEvents(REMOVE_USER, tournamentAggregateId));

            Set<RemoveUserEvent> events = eventRepository.findAll()
                    .stream()
                    .filter(e -> REMOVE_USER.equals(e.getType()))
                    .map(e -> (RemoveUserEvent) e)
                    .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                    .collect(Collectors.toSet());

            for(RemoveUserEvent e : events) {
                Set<Integer> tournamentIdsByUser = tournamentRepository.findAllAggregateIdsByUser(e.getUserAggregateId());
                boolean runningTransactions = unitOfWorkRepository.findRunningTransactions(tournamentAggregateId, e.getAggregateVersion()).size() > 0;
                if(runningTransactions || !tournamentIdsByUser.contains(tournamentAggregateId)) {
                    continue;
                }
                System.out.printf("Processing remove user %d event for tournament %d\n", e.getUserAggregateId(), tournamentAggregateId);
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.removeUser(tournamentAggregateId, e.getUserAggregateId(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);

                tournamentProcessedEvents.addProcessedEventsIds(e.getId());
            }
            tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
        }
    }


    /*
        TOPIC_EXISTS
            t: this.tournamentTopics | t.state != INACTIVE => EXISTS Topic(t.id) && t.name == Topic(t.id).name
    */
    @Scheduled(fixedDelay = 10000)
    public void detectUpdateTopicEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());

        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                    .filter(pe -> tournamentAggregateId.equals(pe.getAggregateId()))
                    .filter(pe -> UPDATE_TOPIC.equals(pe.getEventType()))
                    .findFirst()
                    .orElse(new TournamentProcessedEvents(UPDATE_TOPIC, tournamentAggregateId));

            Set<UpdateTopicEvent> events = eventRepository.findAll()
                    .stream()
                    .filter(e -> UPDATE_TOPIC.equals(e.getType()))
                    .map(e -> (UpdateTopicEvent) e)
                    .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                    .collect(Collectors.toSet());

            for(UpdateTopicEvent e : events) {
                Set<Integer> tournamentIdsByTopic = tournamentRepository.findAllAggregateIdsByTopic(e.getTopicAggregateId());
                boolean runningTransactions = unitOfWorkRepository.findRunningTransactions(tournamentAggregateId, e.getAggregateVersion()).size() > 0;
                if(runningTransactions || !tournamentIdsByTopic.contains(tournamentAggregateId)) {
                    continue;
                }
                System.out.printf("Processing update topic %d event for tournament %d\n", e.getTopicAggregateId(), tournamentAggregateId);
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.updateTopic(tournamentAggregateId, e.getTopicAggregateId(), e.getTopicName(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);

                tournamentProcessedEvents.addProcessedEventsIds(e.getId());
            }
            tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
        }
    }

    /*
        TOPIC_EXISTS
            t: this.tournamentTopics | t.state != INACTIVE => EXISTS Topic(t.id) && t.name == Topic(t.id).name
    */
    @Scheduled(fixedDelay = 10000)
    public void detectDeleteTopicEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());

        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                    .filter(pe -> tournamentAggregateId.equals(pe.getAggregateId()))
                    .filter(pe -> DELETE_TOPIC.equals(pe.getEventType()))
                    .findFirst()
                    .orElse(new TournamentProcessedEvents(DELETE_TOPIC, tournamentAggregateId));

            Set<DeleteTopicEvent> events = eventRepository.findAll()
                    .stream()
                    .filter(e -> DELETE_TOPIC.equals(e.getType()))
                    .map(e -> (DeleteTopicEvent) e)
                    .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                    .collect(Collectors.toSet());

            for(DeleteTopicEvent e : events) {
                Set<Integer> tournamentIdsByTopic = tournamentRepository.findAllAggregateIdsByTopic(e.getTopicAggregateId());
                boolean runningTransactions = unitOfWorkRepository.findRunningTransactions(tournamentAggregateId, e.getAggregateVersion()).size() > 0;
                if(runningTransactions || !tournamentIdsByTopic.contains(tournamentAggregateId)) {
                    continue;
                }
                System.out.printf("Processing remove topic %d event for tournament %d\n", e.getTopicAggregateId(), tournamentAggregateId);
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.removeTopic(tournamentAggregateId, e.getTopicAggregateId(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);

                tournamentProcessedEvents.addProcessedEventsIds(e.getId());
            }
            tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
        }
    }

    /*
        QUIZ_ANSWER_EXISTS
            p: this.participants | (!p.answer.isEmpty && p.answer.state != INACTIVE) => EXISTS QuizAnswer(p.answer.id)
     */
    @Scheduled(fixedDelay = 10000)
    public void detectAnswerQuestionEvent() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());

        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            TournamentProcessedEvents tournamentProcessedEvents = tournamentProcessedEventsRepository.findAll().stream()
                    .filter(pe -> tournamentAggregateId.equals(pe.getAggregateId()))
                    .filter(pe -> ANSWER_QUESTION.equals(pe.getEventType()))
                    .findFirst()
                    .orElse(new TournamentProcessedEvents(ANSWER_QUESTION, tournamentAggregateId));

            Set<AnswerQuestionEvent> events = eventRepository.findAll()
                    .stream()
                    .filter(e -> ANSWER_QUESTION.equals(e.getType()))
                    .map(e -> (AnswerQuestionEvent) e)
                    .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                    .collect(Collectors.toSet());

            for(AnswerQuestionEvent e : events) {
                Set<Integer> tournamentsAggregateIdsByQuiz = tournamentRepository.findAllAggregateIdsByQuiz(e.getQuizAggregateId());
                boolean runningTransactions = unitOfWorkRepository.findRunningTransactions(tournamentAggregateId, e.getAggregateVersion()).size() > 0;
                if(runningTransactions || !tournamentsAggregateIdsByQuiz.contains(tournamentAggregateId)) {
                    continue;
                }
                System.out.printf("Processing answer %d event for tournament %d\n", e.getAnswerAggregateId(), tournamentAggregateId);
                UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
                tournamentService.updateParticipantAnswer(tournamentAggregateId, e.getUserAggregateId(), e.getAnswerAggregateId(), e.isCorrect(), unitOfWork);
                unitOfWorkService.commit(unitOfWork);

                tournamentProcessedEvents.addProcessedEventsIds(e.getId());
            }
            tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
        }
    }
    /*
        QUIZ_EXISTS
            this.tournamentQuiz.state != INACTIVE => EXISTS Quiz(this.tournamentQuiz.id)
    */
}