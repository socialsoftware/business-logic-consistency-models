package pt.ulisboa.tecnico.socialsoftware.blcm.answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.service.AnswerService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.QuestionTopic;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService;

@Service
public class AnswerFunctionalities {

    @Autowired
    private QuizService quizService;

     @Autowired
     private AnswerService answerService;

     @Autowired
     private QuestionService questionService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;



    public void answerQuestion(Integer quizAggregateId, Integer userAggregateId, QuestionAnswerDto userQuestionAnswerDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        QuestionDto questionDto = questionService.getCausalQuestionRemote(userQuestionAnswerDto.getQuestionAggregateId(), unitOfWork);
        answerService.answerQuestion(quizAggregateId, userAggregateId, userQuestionAnswerDto, questionDto, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void startQuiz(Integer quizAggregateId, Integer courseExecutionAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        answerService.startQuiz(quizAggregateId, courseExecutionAggregateId, userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void concludeQuiz(Integer quizAggregateId, Integer courseExecutionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        answerService.concludeQuiz(quizAggregateId, courseExecutionAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void getSolvedQuizzes(Integer userAggregateId, Integer courseExecutionAggregateId) {
        /*UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return answerService.getSolvedQuizzes()*/
    }

    /************************************************ EVENT PROCESSING ************************************************/

    public void processRemoveUser(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing remove user %d event for answer %d\n", eventToProcess.getAggregateId(), aggregateId);
        RemoveUserEvent removeUserEvent = (RemoveUserEvent) eventToProcess;
        answerService.removeUser(aggregateId, removeUserEvent.getAggregateId(), removeUserEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void processRemoveQuestion(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing remove question %d event for answer %d\n", eventToProcess.getAggregateId(), aggregateId);
        RemoveQuestionEvent removeQuestionEvent = (RemoveQuestionEvent) eventToProcess;
        answerService.removeQuestion(aggregateId, removeQuestionEvent.getAggregateId(), removeQuestionEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void processUnenrollStudent(Integer aggregateId, Event eventToProcess) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        System.out.printf("Processing uneroll student from course execution %d event for answer %d\n", eventToProcess.getAggregateId(), aggregateId);
        UnerollStudentFromCourseExecutionEvent unerollStudentFromCourseExecutionEvent = (UnerollStudentFromCourseExecutionEvent) eventToProcess;
        answerService.removeUser(aggregateId, unerollStudentFromCourseExecutionEvent.getAggregateId(), unerollStudentFromCourseExecutionEvent.getAggregateVersion(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }
}
