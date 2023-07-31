package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.QuizCourseExecution;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.service.QuizService;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;

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
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        QuizCourseExecution quizCourseExecution = new QuizCourseExecution(courseExecutionService.getCourseExecutionById(courseExecutionId, unitOfWork));

        Set<QuestionDto> questions = quizDto.getQuestionDtos().stream()
                .map(qq -> questionService.getQuestionById(qq.getAggregateId(), unitOfWork))
                .collect(Collectors.toSet());

        QuizDto quizDto1 = quizService.createQuiz(quizCourseExecution, questions, quizDto, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
        return quizDto1;

    }

    public QuizDto findQuiz(Integer quizAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        return quizService.getQuizById(quizAggregateId, unitOfWork);
    }

    public List<QuizDto> getAvailableQuizzes(Integer userAggregateId, Integer courseExecutionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        return quizService.getAvailableQuizzes(courseExecutionAggregateId, unitOfWork);
    }

    public QuizDto updateQuiz(QuizDto quizDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        Set<QuizQuestion> quizQuestions = quizDto.getQuestionDtos().stream().map(QuizQuestion::new).collect(Collectors.toSet());
        QuizDto quizDto1 = quizService.updateQuiz(quizDto, quizQuestions, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
        return quizDto1;
    }

}
