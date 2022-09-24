package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public QuizDto updateQuiz(QuizDto quizDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        List<QuizQuestion> quizQuestions = quizDto.getQuestionDtos().stream()
                .map(qq -> questionService.getCausalQuestionRemote(qq.getAggregateId(), unitOfWork))
                .map(QuizQuestion::new)
                .collect(Collectors.toList());
        //QuizDto quizDto1 = quizService.updateQuiz(quizDto, quizQuestions, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
        //return quizDto1;
        return null;
    }

    public QuizDto findQuiz(Integer quizAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return quizService.getCausalQuizRemote(quizAggregateId, unitOfWork);
    }
}
