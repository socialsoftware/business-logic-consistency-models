package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEvents;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEventsRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.*;

public class QuizEventDetection {
    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private EventUtils eventUtils;

    @Autowired
    private QuizService quizService;

    @Autowired
    private ProcessedEventsRepository processedEventsRepository;

    @Autowired
    private QuizRepository quizRepository;



    /*
        COURSE_EXECUTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectRemoveCourseExecutionEvents() {
        Set<Integer> quizAggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        List<Event> events = eventUtils.getEmittedEvents(REMOVE_COURSE_EXECUTION);
        for(Integer quizAggregateId : quizAggregateIds) {
            ProcessedEvents processedEvents = eventUtils.getTournamentProcessedEvents(REMOVE_COURSE_EXECUTION, quizAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processRemoveCourseExecutionEvents(quizAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processRemoveCourseExecutionEvents(Integer quizAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<RemoveCourseExecutionEvent> removeCourseExecutionEvents = events.stream()
                .map(e -> RemoveCourseExecutionEvent.class.cast(e))
                .collect(Collectors.toSet());
        for(RemoveCourseExecutionEvent e : removeCourseExecutionEvents) {
            Set<Integer> quizIdsByCourseExecution = quizRepository.findAllAggregateIdsByCourseExecution(e.getAggregateId());
            if(!quizIdsByCourseExecution.contains(quizAggregateId)) {
                continue;
            }
            System.out.printf("Processing remove course execution %d event for tournament %d\n", e.getAggregateId(), quizAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Quiz updatedQuiz = quizService.removeCourseExecution(quizAggregateId, e.getAggregateId(), e.getAggregateVersion(), unitOfWork);
            if(updatedQuiz != null) {
                updatedQuiz.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectUpdateQuestionEvent() {
        Set<Integer> quizAggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        List<Event> events = eventUtils.getEmittedEvents(UPDATE_QUESTION);
        for(Integer quizAggregateId : quizAggregateIds) {
            ProcessedEvents processedEvents = eventUtils.getTournamentProcessedEvents(UPDATE_QUESTION, quizAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processUpdateQuestionEvents(quizAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processUpdateQuestionEvents(Integer quizAggregateId, List<Event> events) {
        Set<Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<UpdateQuestionEvent> removeQuestionEvents = events.stream()
                .map(e -> UpdateQuestionEvent.class.cast(e))
                .collect(Collectors.toSet());
        for(UpdateQuestionEvent e : removeQuestionEvents) {
            Set<Integer> quizIdsByQuestion = quizRepository.findAllAggregateIdsByQuestion(e.getAggregateId());
            if(!quizIdsByQuestion.contains(quizAggregateId)) {
                continue;
            }
            System.out.printf("Processing update question %d event for quiz %d\n", e.getAggregateId(), quizAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Quiz updatedQuiz = quizService.updateQuestion(quizAggregateId, e.getAggregateId(), e.getTitle(), e.getContent(), e.getAggregateVersion(), unitOfWork);
            if(updatedQuiz != null) {
                updatedQuiz.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
    }

    /*
        QUESTION_EXISTS
     */
    @Scheduled(fixedDelay = 1000)
    public void detectRemoveQuestionEvent() {
        Set<Integer> quizAggregateIds = quizRepository.findAll().stream().map(Quiz::getAggregateId).collect(Collectors.toSet());
        List<Event> events = eventUtils.getEmittedEvents(REMOVE_QUESTION);
        for(Integer quizAggregateId : quizAggregateIds) {
            ProcessedEvents processedEvents = eventUtils.getTournamentProcessedEvents(REMOVE_QUESTION, quizAggregateId);
            events = events.stream()
                    .filter(e -> !(processedEvents.containsEventVersion(e.getAggregateVersion())))
                    .collect(Collectors.toList());
            Set<Integer> newlyProcessedEventVersions = processRemoveQuestionEvents(quizAggregateId, events);
            newlyProcessedEventVersions.forEach(ev -> processedEvents.addProcessedEventVersion(ev));
            processedEventsRepository.save(processedEvents);
        }
    }

    private Set<Integer> processRemoveQuestionEvents(java.lang.Integer quizAggregateId, List<Event> events) {
        Set<java.lang.Integer> newlyProcessedEventVersions = new HashSet<>();
        Set<RemoveQuestionEvent> removeQuestionEvents = events.stream()
                .map(e -> RemoveQuestionEvent.class.cast(e))
                .collect(Collectors.toSet());
        for(RemoveQuestionEvent e : removeQuestionEvents) {
            Set<Integer> tournamentIdsByCourseExecution = quizRepository.findAllAggregateIdsByQuestion(e.getAggregateId());
            if(!tournamentIdsByCourseExecution.contains(quizAggregateId)) {
                continue;
            }
            System.out.printf("Processing remove question %d event for quiz %d\n", e.getAggregateId(), quizAggregateId);
            UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
            Quiz updatedQuiz = quizService.removeQuestion(quizAggregateId, e.getAggregateId(), e.getAggregateVersion(), unitOfWork);
            if(updatedQuiz != null) {
                updatedQuiz.addProcessedEvent(e.getType(), e.getAggregateVersion());
                unitOfWorkService.commit(unitOfWork);
            }
            newlyProcessedEventVersions.add(e.getAggregateVersion());
        }
        return newlyProcessedEventVersions;
    }
}
