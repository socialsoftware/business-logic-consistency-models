package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.QuizCourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuizFunctionalities {

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private CourseExecutionService courseExecutionService;

    @Autowired
    private QuestionService questionService;
    public QuizDto createQuiz(Integer courseExecutionId, QuizDto quizDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        QuizCourseExecution quizCourseExecution = new QuizCourseExecution(courseExecutionService.getCausalCourseExecutionRemote(courseExecutionId, unitOfWork));
        Set<QuizQuestion> quizQuestions = quizDto.getQuestionDtos().stream()
                .map(qq -> questionService.getCausalQuestionRemote(qq.getAggregateId(), unitOfWork))
                .map(QuizQuestion::new)
                .collect(Collectors.toSet());

        QuizDto quizDto1 = quizService.createQuiz(quizCourseExecution, quizQuestions, quizDto, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
        return quizDto1;

    }

    public QuizDto findQuiz(Integer quizAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return quizService.getCausalQuizRemote(quizAggregateId, unitOfWork);
    }

    public List<QuizDto> getAvailableQuizzes(Integer userAggregateId, Integer courseExecutionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return quizService.getAvailableQuizzes(courseExecutionAggregateId, unitOfWork);
    }

    public QuizDto updateQuiz(QuizDto quizDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        Set<QuizQuestion> quizQuestions = quizDto.getQuestionDtos().stream().map(QuizQuestion::new).collect(Collectors.toSet());
        QuizDto quizDto1 = quizService.updateQuiz(quizDto, quizQuestions, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
        return quizDto1;
    }

    /************************************************ EVENT PROCESSING ************************************************/

    public void processRemoveCourseExecutionEvent(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing remove course execution %d event for quiz %d\n", eventToProcess.getAggregateId(), aggregateId);
        RemoveCourseExecutionEvent removeCourseExecutionEvent = (RemoveCourseExecutionEvent) eventToProcess;
        quizService.removeCourseExecution(aggregateId, removeCourseExecutionEvent.getAggregateId(), removeCourseExecutionEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void processUpdateQuestion(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing update question execution %d event for quiz %d\n", eventToProcess.getAggregateId(), aggregateId);
        UpdateQuestionEvent updateQuestionEvent = (UpdateQuestionEvent) eventToProcess;
        quizService.updateQuestion(aggregateId, updateQuestionEvent.getAggregateId(), updateQuestionEvent.getTitle(), updateQuestionEvent.getContent(), updateQuestionEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void processRemoveQuestion(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing remove question execution %d event for quiz %d\n", eventToProcess.getAggregateId(), aggregateId);
        RemoveQuestionEvent removeQuestionEvent = (RemoveQuestionEvent) eventToProcess;
        quizService.removeQuestion(aggregateId, removeQuestionEvent.getAggregateId(), removeQuestionEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}
