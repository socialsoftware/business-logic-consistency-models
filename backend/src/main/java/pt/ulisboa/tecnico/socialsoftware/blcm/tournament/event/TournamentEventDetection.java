package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEvents;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEventsRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;

import java.util.Comparator;
import java.util.HashSet;
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
    private ProcessedEventsRepository processedEventsRepository;

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
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        List<Event> events = getEmittedEvents(ANONYMIZE_EXECUTION_STUDENT);
        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            ProcessedEvents processedEvents = getTournamentProcessedEvents(ANONYMIZE_EXECUTION_STUDENT, tournamentAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());

            Set<Integer> newlyProcessedEventVersions = processAnonymizeUserEvents(tournamentAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processAnonymizeUserEvents(Integer tournamentAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<AnonymizeExecutionStudentEvent> anonymizeExecutionStudentEvents = events.stream()
                .map(AnonymizeExecutionStudentEvent.class::cast)
                .collect(Collectors.toSet());
        for(AnonymizeExecutionStudentEvent e : anonymizeExecutionStudentEvents) {
            Set<Integer> tournamentIdsByExecutionAndUser = tournamentRepository.findAllAggregateIdsByExecutionAndUser(e.getExecutionAggregateId(), e.getAggregateId());
            if(!tournamentIdsByExecutionAndUser.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing anonymize user %d event for tournament %d\n", e.getAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Tournament updatedTournament = tournamentService.anonymizeUser(tournamentAggregateId, e.getAggregateId(), e.getName(), e.getUsername(), e.getAggregateVersion(), unitOfWork);
            if(updatedTournament != null) {
                updatedTournament.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
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
        List<Event> events = getEmittedEvents(REMOVE_COURSE_EXECUTION);
        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            ProcessedEvents processedEvents = getTournamentProcessedEvents(REMOVE_COURSE_EXECUTION, tournamentAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processRemoveCourseExecutionEvents(tournamentAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processRemoveCourseExecutionEvents(Integer tournamentAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<RemoveCourseExecutionEvent> removeCourseExecutionEvents = events.stream()
                .map(RemoveCourseExecutionEvent.class::cast)
                .collect(Collectors.toSet());
        for(RemoveCourseExecutionEvent e : removeCourseExecutionEvents) {
            Set<Integer> tournamentIdsByCourseExecution = tournamentRepository.findAllAggregateIdsByCourseExecution(e.getAggregateId());
            if(!tournamentIdsByCourseExecution.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing remove course execution %d event for tournament %d\n", e.getAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Tournament updatedTournament = tournamentService.removeCourseExecution(tournamentAggregateId, e.getAggregateId(), e.getAggregateVersion(), unitOfWork);
            if(updatedTournament != null) {
                updatedTournament.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
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

    @Scheduled(fixedDelay = 1000)
    public void detectRemoveUserEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        List<Event> events = getEmittedEvents(REMOVE_USER);
        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            ProcessedEvents processedEvents = getTournamentProcessedEvents(REMOVE_USER, tournamentAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processRemoveUserEvents(tournamentAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processRemoveUserEvents(Integer tournamentAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<RemoveUserEvent> removeUserEvents = events.stream()
                .map(RemoveUserEvent.class::cast)
                .collect(Collectors.toSet());
        for(RemoveUserEvent e : removeUserEvents) {
            Set<Integer> tournamentIdsByUser = tournamentRepository.findAllAggregateIdsByUser(e.getAggregateId());
            if(!tournamentIdsByUser.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing remove user %d event for tournament %d\n", e.getAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Tournament updatedTournament = tournamentService.removeUser(tournamentAggregateId, e.getAggregateId(), e.getAggregateVersion(), unitOfWork);
            if(updatedTournament != null) {
                updatedTournament.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());

        }
        return newlyProcessedEventVersions;
    }

    /*
        TOPIC_EXISTS
            t: this.tournamentTopics | t.state != INACTIVE => EXISTS Topic(t.id) && t.name == Topic(t.id).name
    */
    @Scheduled(fixedDelay = 100000)
    public void detectUpdateTopicEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        List<Event> events = getEmittedEvents(UPDATE_TOPIC);
        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            ProcessedEvents processedEvents = getTournamentProcessedEvents(UPDATE_TOPIC, tournamentAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processUpdateTopicEvents(tournamentAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processUpdateTopicEvents(Integer tournamentAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<UpdateTopicEvent> updateTopicEvents = events.stream()
                .map(UpdateTopicEvent.class::cast)
                .collect(Collectors.toSet());
        for(UpdateTopicEvent e : updateTopicEvents) {
            Set<Integer> tournamentIdsByTopic = tournamentRepository.findAllAggregateIdsByTopic(e.getAggregateId());
            if(!tournamentIdsByTopic.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing update topic %d event for tournament %d\n", e.getAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Tournament updatedTournament = tournamentService.updateTopic(tournamentAggregateId, e.getAggregateId(), e.getTopicName(), e.getAggregateVersion(), unitOfWork);
            if(updatedTournament != null) {
                updatedTournament.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
    }

    /*
        TOPIC_EXISTS
            t: this.tournamentTopics | t.state != INACTIVE => EXISTS Topic(t.id) && t.name == Topic(t.id).name
    */
    @Scheduled(fixedDelay = 1000)
    public void detectDeleteTopicEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        List<Event> events = getEmittedEvents(DELETE_TOPIC);
        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            ProcessedEvents processedEvents = getTournamentProcessedEvents(DELETE_TOPIC, tournamentAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processDeleteTopicEvents(tournamentAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processDeleteTopicEvents(Integer tournamentAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<DeleteTopicEvent> deleteTopicEvents = events.stream()
                .map(DeleteTopicEvent.class::cast)
                .collect(Collectors.toSet());
        for(DeleteTopicEvent e : deleteTopicEvents) {
            Set<Integer> tournamentIdsByTopic = tournamentRepository.findAllAggregateIdsByTopic(e.getAggregateId());
            if(!tournamentIdsByTopic.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing remove topic %d event for tournament %d\n", e.getAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Tournament updatedTournament = tournamentService.removeTopic(tournamentAggregateId, e.getAggregateId(), e.getAggregateVersion(), unitOfWork);
            if(updatedTournament != null) {
                updatedTournament.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
    }

    /*
        QUIZ_ANSWER_EXISTS
            p: this.participants | (!p.answer.isEmpty && p.answer.state != INACTIVE) => EXISTS QuizAnswer(p.answer.id)
     */
    @Scheduled(fixedDelay = 1000)
    public void detectAnswerQuestionEvent() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        List<Event> events = getEmittedEvents(ANSWER_QUESTION);
        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            ProcessedEvents processedEvents = getTournamentProcessedEvents(ANSWER_QUESTION, tournamentAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processAnswerQuestionEvents(tournamentAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processAnswerQuestionEvents(Integer tournamentAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<AnswerQuestionEvent> answerQuestionEvents = events.stream()
                .map(AnswerQuestionEvent.class::cast)
                .collect(Collectors.toSet());
        for(AnswerQuestionEvent e : answerQuestionEvents) {
            Set<Integer> tournamentsAggregateIdsByQuiz = tournamentRepository.findAllAggregateIdsByQuiz(e.getQuizAggregateId());
            if(!tournamentsAggregateIdsByQuiz.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing answer %d event for tournament %d\n", e.getAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Tournament updatedTournament = tournamentService.updateParticipantAnswer(tournamentAggregateId, e.getUserAggregateId(), e.getAggregateId(), e.isCorrect(), e.getAggregateVersion(), unitOfWork);
            if(updatedTournament != null) {
                updatedTournament.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
    }

    /*
        CREATOR_COURSE_EXECUTION
        PARTICIPANT_COURSE_EXECUTION
     */
    
    @Scheduled(fixedDelay = 1000)
    public void detectUnenrollStudentFromCourseExecutionEvents() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        List<Event> events = getEmittedEvents(UNENROLL_STUDENT);
        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            ProcessedEvents processedEvents = getTournamentProcessedEvents(UNENROLL_STUDENT, tournamentAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processUnenrollStudentEvent(tournamentAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processUnenrollStudentEvent(Integer tournamentAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<UnerollStudentFromCourseExecutionEvent> removeQuestionEvents = events.stream()
                .map(UnerollStudentFromCourseExecutionEvent.class::cast)
                .collect(Collectors.toSet());
        for(UnerollStudentFromCourseExecutionEvent e : removeQuestionEvents) {
            Set<Integer> tournamentIdsIdsByUser = tournamentRepository.findAllAggregateIdsByUser(e.getAggregateId());
            if(!tournamentIdsIdsByUser.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing unenroll student %d event for tournament %d\n", e.getAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Tournament updatedTournament = tournamentService.removeUser(tournamentAggregateId, e.getAggregateId(), e.getAggregateVersion(), unitOfWork);
            if(updatedTournament != null) {
                updatedTournament.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
    }

    @Scheduled(fixedDelay = 1000)
    public void detectInvalidateQuizEvent() {
        Set<Integer> tournamentAggregateIds = tournamentRepository.findAll().stream().map(Tournament::getAggregateId).collect(Collectors.toSet());
        List<Event> events = getEmittedEvents(INVALIDATE_QUIZ);
        for(Integer tournamentAggregateId : tournamentAggregateIds) {
            ProcessedEvents processedEvents = getTournamentProcessedEvents(INVALIDATE_QUIZ, tournamentAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processUnenrollStudentEvent(tournamentAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processInvalidateQuizEvent(Integer tournamentAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<InvalidateQuizEvent> removeQuestionEvents = events.stream()
                .map(InvalidateQuizEvent.class::cast)
                .collect(Collectors.toSet());
        for(InvalidateQuizEvent e : removeQuestionEvents) {
            Set<Integer> tournamentIdsIdsByUser = tournamentRepository.findAllAggregateIdsByQuiz(e.getAggregateId());
            if(!tournamentIdsIdsByUser.contains(tournamentAggregateId)) {
                continue;
            }
            System.out.printf("Processing invalidate %d event for tournament %d\n", e.getAggregateId(), tournamentAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Tournament updatedTournament = tournamentService.invalidateQuiz(tournamentAggregateId, e.getAggregateId(), e.getAggregateVersion(), unitOfWork);
            if(updatedTournament != null) {
                updatedTournament.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
    }

    private List<Event> getEmittedEvents(String eventType) {
        return eventRepository.findAll()
                .stream()
                .filter(e -> eventType.equals(e.getType()))
                .distinct()
                .sorted(Comparator.comparing(Event::getTs).reversed())
                .collect(Collectors.toList());
    }

    private ProcessedEvents getTournamentProcessedEvents(String eventType, Integer tournamentAggregateId) {
        return processedEventsRepository.findAll().stream()
                .filter(pe -> tournamentAggregateId.equals(pe.getAggregateId()))
                .filter(pe -> eventType.equals(pe.getEventType()))
                .findFirst()
                .orElse(new ProcessedEvents(eventType, tournamentAggregateId));
    }


    /*
        QUIZ_EXISTS
            this.tournamentQuiz.state != INACTIVE => EXISTS Quiz(this.tournamentQuiz.id)
    */
}