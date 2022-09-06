package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Service
public class QuizService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CourseExecutionService courseExecutionService;

    @Transactional
   public QuizDto getCausalQuiz(Integer aggregateId) {
        // TODO
        return new QuizDto(new Quiz());
    }

    @Transactional
    public QuizDto getCausalQuizRemote(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        return new QuizDto(getCausalQuizLocal(aggregateId, unitOfWorkWorkService));
    }

    // intended for requests from local functionalities
    public Quiz getCausalQuizLocal(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        Quiz quiz = quizRepository.findByAggregateIdAndVersion(aggregateId, unitOfWorkWorkService.getVersion())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, aggregateId));

        if(quiz.getState().equals(DELETED)) {
            throw new TutorException(TOURNAMENT_DELETED, quiz.getAggregateId());
        }

        quiz.checkDependencies(unitOfWorkWorkService);
        return quiz;
    }

    // TODO discuss this implementation
    @Transactional
    public QuizDto generateQuiz(Integer courseExecutionAggregateId, QuizDto quizDto, List<QuestionDto> questionDtos, Integer numberOfQuestions, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();

        QuizCourseExecution quizCourseExecution = new QuizCourseExecution(courseExecutionService.getCausalCourseExecutionRemote(courseExecutionAggregateId, unitOfWork));

        if(questionDtos.size() < numberOfQuestions) {
            throw new TutorException(ErrorMessage.NOT_ENOUGH_QUESTIONS);
        }

        Set<Integer> questionPositions = new HashSet<>();
        while(questionPositions.size() < numberOfQuestions) {
            questionPositions.add(ThreadLocalRandom.current().nextInt(0, questionDtos.size()));
        }

        List<QuizQuestion> quizQuestions = questionPositions.stream()
                .map(pos -> questionDtos.get(pos))
                .map(QuizQuestion::new)
                .collect(Collectors.toList());


        Quiz quiz = new Quiz(aggregateId, quizCourseExecution, quizQuestions, quizDto);
        unitOfWork.addUpdatedObject(quiz);
        return new QuizDto(quiz);
    }

    @Transactional
    public QuizDto startTournamentQuiz(Integer userAggregateId, Integer quizAggregateId, UnitOfWork unitOfWork) {
        /* must add more verifications */
        Quiz oldQuiz = quizRepository.findByAggregateIdAndVersion(quizAggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(QUIZ_NOT_FOUND, quizAggregateId));

        Quiz newQuiz = new Quiz(oldQuiz);

        /*do stuff to the new quiz*/

        unitOfWork.addUpdatedObject(newQuiz);
        return new QuizDto(oldQuiz);
    }

    @Transactional
    public QuizDto createQuiz(QuizCourseExecution quizCourseExecution, List<QuizQuestion> quizQuestions, QuizDto quizDto, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        Quiz quiz = new Quiz(aggregateId, quizCourseExecution, quizQuestions, quizDto);
        unitOfWork.addUpdatedObject(quiz);
        return new QuizDto(quiz);
    }

    @Transactional
    public QuizDto updateQuiz(QuizDto quizDto, List<QuestionDto> questionDtos, UnitOfWork unitOfWork) {
        Quiz oldQuiz = getCausalQuizLocal(quizDto.getAggregateId(), unitOfWork);
        Quiz newQuiz = new Quiz(oldQuiz);
        newQuiz.update(quizDto);

        List<QuizQuestion> quizQuestions = questionDtos.stream()
                .map(QuizQuestion::new)
                .collect(Collectors.toList());

        if(quizQuestions != null) {
            newQuiz.setQuizQuestions(quizQuestions);
        }

        unitOfWork.addUpdatedObject(newQuiz);
        return new QuizDto(newQuiz);
    }
}
