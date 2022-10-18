package pt.ulisboa.tecnico.socialsoftware.blcm.answer.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.Answer;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.repository.AnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.service.AnswerService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEvents;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEventsRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.*;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.REMOVE_COURSE_EXECUTION;

@Component
public class AnswerEventDetection {

    @Autowired
    private EventUtils eventUtils;

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
        Set<Integer> answerAggregateIds = answerRepository.findAll().stream().map(Answer::getAggregateId).collect(Collectors.toSet());
        List<Event> events = getEmittedEvents(REMOVE_USER);
        for(Integer answerAggregateId : answerAggregateIds) {
            ProcessedEvents processedEvents = getTournamentProcessedEvents(REMOVE_USER, answerAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processRemoveUserEvents(answerAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processRemoveUserEvents(Integer answerAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<RemoveUserEvent> removeUserEvents = events.stream()
                .map(e -> RemoveUserEvent.class.cast(e))
                .collect(Collectors.toSet());
        for(RemoveUserEvent e : removeUserEvents) {
            Set<Integer> answerIdsByUser = answerRepository.findAllAggregateIdsByUser(e.getAggregateId());
            if(!answerIdsByUser.contains(answerAggregateId)) {
                continue;
            }
            System.out.printf("Processing remove user %d event for answer %d\n", e.getAggregateId(), answerAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Answer updateAnswer = answerService.removeUser(answerAggregateId, e.getAggregateId(), e.getAggregateVersion(), unitOfWork);
            if(updateAnswer != null) {
                updateAnswer.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());

        }
        return newlyProcessedEventVersions;
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
        Set<Integer> answerAggregateIds = answerRepository.findAll().stream().map(Answer::getAggregateId).collect(Collectors.toSet());
        List<Event> events = eventUtils.getEmittedEvents(REMOVE_QUESTION);
        for(Integer answerAggregateId : answerAggregateIds) {
            ProcessedEvents processedEvents = eventUtils.getTournamentProcessedEvents(REMOVE_QUESTION, answerAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processRemoveQuestionEvents(answerAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processRemoveQuestionEvents(Integer answerAggregateId, List<Event> events) {
        Set<java.lang.Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<RemoveQuestionEvent> removeQuestionEvents = events.stream()
                .map(e -> RemoveQuestionEvent.class.cast(e))
                .collect(Collectors.toSet());
        for(RemoveQuestionEvent e : removeQuestionEvents) {
            Set<Integer> answerIdsByQuestion = answerRepository.findAllAggregateIdsByQuestion(e.getAggregateId());
            if(!answerIdsByQuestion.contains(answerAggregateId)) {
                continue;
            }
            System.out.printf("Processing remove question %d event for answer %d\n", e.getAggregateId(), answerAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Answer updatedAnswer = answerService.removeQuestion(answerAggregateId, e.getAggregateId(), e.getAggregateVersion(), unitOfWork);
            if(updatedAnswer != null) {
                updatedAnswer.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
    }

    /*
    QUIZ_COURSE_EXECUTION_SAME_AS_USER
     */

    @Scheduled(fixedDelay = 1000)
    public void detectUnenrollStudentEvent() {
        Set<Integer> answerAggregateIds = answerRepository.findAll().stream().map(Answer::getAggregateId).collect(Collectors.toSet());
        List<Event> events = eventUtils.getEmittedEvents(UNENROLL_STUDENT);
        for(Integer answerAggregateId : answerAggregateIds) {
            ProcessedEvents processedEvents = eventUtils.getTournamentProcessedEvents(UNENROLL_STUDENT, answerAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processRemoveQuestionEvents(answerAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processUnenrollStudentEvent(Integer answerAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<UnerollStudentFromCourseExecutionEvent> removeQuestionEvents = events.stream()
                .map(e -> UnerollStudentFromCourseExecutionEvent.class.cast(e))
                .collect(Collectors.toSet());
        for(UnerollStudentFromCourseExecutionEvent e : removeQuestionEvents) {
            Set<Integer> answerIdsByUser = answerRepository.findAllAggregateIdsByUser(e.getAggregateId());
            if(!answerIdsByUser.contains(answerAggregateId)) {
                continue;
            }
            System.out.printf("Processing unenroll student %d event for answer %d\n", e.getAggregateId(), answerAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Answer updatedAnswer = answerService.removeUser(answerAggregateId, e.getAggregateId(), e.getAggregateVersion(), unitOfWork);
            if(updatedAnswer != null) {
                updatedAnswer.addProcessedEvent(e.getType(), e.getAggregateVersion());
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
}
