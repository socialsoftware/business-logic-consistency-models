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

import java.util.Comparator;
import java.util.List;
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
            TournamentProcessedEvents tournamentProcessedEvents = getTournamentProcessedEvents(ANONYMIZE_USER, tournamentAggregateId);
            List<DomainEvent> events = getDomainEvents(ANONYMIZE_USER, tournamentProcessedEvents);

            processAnonymizeUserEvents(tournamentAggregateId, tournamentProcessedEvents, events);
            tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
        }
    }

    private void processAnonymizeUserEvents(Integer tournamentAggregateId, TournamentProcessedEvents tournamentProcessedEvents, List<DomainEvent> events) {
        Set<AnonymizeUserEvent> anonymizeUserEvents = events.stream().map(e -> (AnonymizeUserEvent) e).collect(Collectors.toSet());
        for(AnonymizeUserEvent e : anonymizeUserEvents) {
            Set<Integer> tournamentIdsByUser = tournamentRepository.findAllAggregateIdsByUser(e.getUserAggregateId());
            boolean runningTransactions = unitOfWorkRepository.findRunningTransactions(tournamentAggregateId, e.getAggregateVersion()).size() > 0;
            if(runningTransactions || !tournamentIdsByUser.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing anonymize user %d event for tournament %d\n", e.getUserAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            tournamentService.anonymizeUser(tournamentAggregateId, e.getUserAggregateId(), e.getName(), e.getUsername(), e.getAggregateVersion(), unitOfWork);
            unitOfWorkService.commit(unitOfWork);

            tournamentProcessedEvents.addProcessedEventsIds(e.getId());
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
            TournamentProcessedEvents tournamentProcessedEvents = getTournamentProcessedEvents(REMOVE_COURSE_EXECUTION, tournamentAggregateId);
            List<DomainEvent> events = getDomainEvents(REMOVE_COURSE_EXECUTION, tournamentProcessedEvents);
            processRemoveCourseExecutionEvents(tournamentAggregateId, tournamentProcessedEvents, events);
            tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
        }
    }

    private void processRemoveCourseExecutionEvents(Integer tournamentAggregateId, TournamentProcessedEvents tournamentProcessedEvents, List<DomainEvent> events) {
        Set<RemoveCourseExecutionEvent> removeCourseExecutionEvents = events.stream().map(e -> (RemoveCourseExecutionEvent) e).collect(Collectors.toSet());
        for(RemoveCourseExecutionEvent e : removeCourseExecutionEvents) {
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
            TournamentProcessedEvents tournamentProcessedEvents = getTournamentProcessedEvents(REMOVE_USER, tournamentAggregateId);
            List<DomainEvent> events = getDomainEvents(REMOVE_USER, tournamentProcessedEvents);
            processRemoveUserEvents(tournamentAggregateId, tournamentProcessedEvents, events);
            tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
        }
    }

    private void processRemoveUserEvents(Integer tournamentAggregateId, TournamentProcessedEvents tournamentProcessedEvents, List<DomainEvent> events) {
        Set<RemoveUserEvent> removeUserEvents = events.stream().map(e -> (RemoveUserEvent) e).collect(Collectors.toSet());
        for(RemoveUserEvent e : removeUserEvents) {
            Set<Integer> tournamentIdsByUser = tournamentRepository.findAllAggregateIdsByUser(e.getUserAggregateId());
            boolean runningTransactions = unitOfWorkRepository.findRunningTransactions(tournamentAggregateId, e.getAggregateVersion()).size() > 0;
            if(runningTransactions || !tournamentIdsByUser.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing remove user %d event for tournament %d\n", e.getUserAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            tournamentService.removeUser(tournamentAggregateId, e.getUserAggregateId(), e.getAggregateVersion(), unitOfWork);
            unitOfWorkService.commit(unitOfWork);

            tournamentProcessedEvents.addProcessedEventsIds(e.getId());
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
            TournamentProcessedEvents tournamentProcessedEvents = getTournamentProcessedEvents(UPDATE_TOPIC, tournamentAggregateId);
            List<DomainEvent> events = getDomainEvents(UPDATE_TOPIC, tournamentProcessedEvents);
            processUpdateTopicEvents(tournamentAggregateId, tournamentProcessedEvents, events);
            tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
        }
    }

    private void processUpdateTopicEvents(Integer tournamentAggregateId, TournamentProcessedEvents tournamentProcessedEvents, List<DomainEvent> events) {
        Set<UpdateTopicEvent> updateTopicEvents = events.stream().map(e -> (UpdateTopicEvent) e).collect(Collectors.toSet());
        for(UpdateTopicEvent e : updateTopicEvents) {
            Set<Integer> tournamentIdsByTopic = tournamentRepository.findAllAggregateIdsByTopic(e.getTopicAggregateId());
            boolean runningTransactions = unitOfWorkRepository.findRunningTransactions(tournamentAggregateId, e.getAggregateVersion()).size() > 0;
            if(runningTransactions || !tournamentIdsByTopic.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing update topic %d event for tournament %d\n", e.getTopicAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            tournamentService.updateTopic(tournamentAggregateId, e.getTopicAggregateId(), e.getTopicName(), e.getAggregateVersion(), unitOfWork);
            unitOfWorkService.commit(unitOfWork);

            tournamentProcessedEvents.addProcessedEventsIds(e.getId());
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
            TournamentProcessedEvents tournamentProcessedEvents = getTournamentProcessedEvents(DELETE_TOPIC, tournamentAggregateId);
            List<DomainEvent> events = getDomainEvents(DELETE_TOPIC, tournamentProcessedEvents);
            processDeleteTopicEvents(tournamentAggregateId, tournamentProcessedEvents, events);
            tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
        }
    }

    private void processDeleteTopicEvents(Integer tournamentAggregateId, TournamentProcessedEvents tournamentProcessedEvents, List<DomainEvent> events) {
        Set<DeleteTopicEvent> deleteTopicEvents = events.stream().map(e -> (DeleteTopicEvent) e).collect(Collectors.toSet());
        for(DeleteTopicEvent e : deleteTopicEvents) {
            Set<Integer> tournamentIdsByTopic = tournamentRepository.findAllAggregateIdsByTopic(e.getTopicAggregateId());
            boolean runningTransactions = unitOfWorkRepository.findRunningTransactions(tournamentAggregateId, e.getAggregateVersion()).size() > 0;
            if(runningTransactions || !tournamentIdsByTopic.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing remove topic %d event for tournament %d\n", e.getTopicAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            tournamentService.removeTopic(tournamentAggregateId, e.getTopicAggregateId(), e.getAggregateVersion(), unitOfWork);
            unitOfWorkService.commit(unitOfWork);

            tournamentProcessedEvents.addProcessedEventsIds(e.getId());
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
            TournamentProcessedEvents tournamentProcessedEvents = getTournamentProcessedEvents(ANSWER_QUESTION, tournamentAggregateId);
            List<DomainEvent> events = getDomainEvents(ANSWER_QUESTION, tournamentProcessedEvents);
            processAnswerQuestionEvents(tournamentAggregateId, tournamentProcessedEvents, events);
            tournamentProcessedEventsRepository.save(tournamentProcessedEvents);
        }
    }

    private void processAnswerQuestionEvents(Integer tournamentAggregateId, TournamentProcessedEvents tournamentProcessedEvents, List<DomainEvent> events) {
        Set<AnswerQuestionEvent> answerQuestionEvents = events.stream().map(e -> (AnswerQuestionEvent) e).collect(Collectors.toSet());
        for(AnswerQuestionEvent e : answerQuestionEvents) {
            Set<Integer> tournamentsAggregateIdsByQuiz = tournamentRepository.findAllAggregateIdsByQuiz(e.getQuizAggregateId());
            boolean runningTransactions = unitOfWorkRepository.findRunningTransactions(tournamentAggregateId, e.getAggregateVersion()).size() > 0;
            if(runningTransactions || !tournamentsAggregateIdsByQuiz.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing answer %d event for tournament %d\n", e.getAnswerAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            tournamentService.updateParticipantAnswer(tournamentAggregateId, e.getUserAggregateId(), e.getAnswerAggregateId(), e.isCorrect(), e.getAggregateVersion(), unitOfWork);
            unitOfWorkService.commit(unitOfWork);

            tournamentProcessedEvents.addProcessedEventsIds(e.getId());
        }
    }

    private List<DomainEvent> getDomainEvents(String eventType, TournamentProcessedEvents tournamentProcessedEvents) {
        return eventRepository.findAll()
                .stream()
                .filter(e -> eventType.equals(e.getType()))
                .filter(e -> !(tournamentProcessedEvents.containsEvent(e.getId())))
                .distinct()
                .sorted(Comparator.comparing(DomainEvent::getTs).reversed())
                .collect(Collectors.toList());
    }

    private TournamentProcessedEvents getTournamentProcessedEvents(String eventType, Integer tournamentAggregateId) {
        return tournamentProcessedEventsRepository.findAll().stream()
                .filter(pe -> tournamentAggregateId.equals(pe.getAggregateId()))
                .filter(pe -> eventType.equals(pe.getEventType()))
                .findFirst()
                .orElse(new TournamentProcessedEvents(eventType, tournamentAggregateId));
    }


    /*
        QUIZ_EXISTS
            this.tournamentQuiz.state != INACTIVE => EXISTS Quiz(this.tournamentQuiz.id)
    */
}