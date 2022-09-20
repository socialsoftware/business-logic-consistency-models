package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.QuizCourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;
import static pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.QuizType.GENERATED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.QuizType.IN_CLASS;

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

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizDto getCausalQuizRemote(Integer aggregateId, UnitOfWork unitOfWork) {
        return new QuizDto(getCausalQuizLocal(aggregateId, unitOfWork));
    }

    // intended for requests from local functionalities
    public Quiz getCausalQuizLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        Quiz quiz = quizRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(QUIZ_NOT_FOUND, aggregateId));

        if(quiz.getState().equals(DELETED)) {
            throw new TutorException(QUIZ_DELETED, quiz.getAggregateId());
        }

        unitOfWork.addToCausalSnapshot(quiz);
        return quiz;
    }

    // TODO discuss this implementation
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
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


        Quiz quiz = new Quiz(aggregateId, quizCourseExecution, quizQuestions, quizDto, GENERATED);
        quiz.setTitle("Generated Quiz Title");
        quiz.setCourseExecution(quizCourseExecution);
        unitOfWork.addUpdatedObject(quiz);
        return new QuizDto(quiz);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizDto startTournamentQuiz(Integer userAggregateId, Integer quizAggregateId, UnitOfWork unitOfWork) {
        /* must add more verifications */
        Quiz oldQuiz = quizRepository.findCausal(quizAggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(QUIZ_NOT_FOUND, quizAggregateId));

        Quiz newQuiz = new Quiz(oldQuiz);

        /*do stuff to the new quiz*/

        unitOfWork.addUpdatedObject(newQuiz);
        return new QuizDto(oldQuiz);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizDto createQuiz(QuizCourseExecution quizCourseExecution, List<QuizQuestion> quizQuestions, QuizDto quizDto, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        Quiz quiz = new Quiz(aggregateId, quizCourseExecution, quizQuestions, quizDto, IN_CLASS);
        unitOfWork.addUpdatedObject(quiz);
        return new QuizDto(quiz);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizDto updateGeneratedQuiz(QuizDto quizDto, Set<Integer> topicsAggregateIds, Integer numberOfQuestions, UnitOfWork unitOfWork) {
        Quiz oldQuiz = getCausalQuizLocal(quizDto.getAggregateId(), unitOfWork);
        Quiz newQuiz = new Quiz(oldQuiz);
        newQuiz.update(quizDto);

        List<QuestionDto> questionDtos = questionService.findQuestionsByTopics(new ArrayList<>(topicsAggregateIds), unitOfWork);

        if(questionDtos.size() < numberOfQuestions) {
            throw new TutorException(ErrorMessage.NOT_ENOUGH_QUESTIONS);
        }

        List<QuizQuestion> quizQuestions = questionDtos.stream()
                .map(QuizQuestion::new)
                .collect(Collectors.toList());

        if(quizQuestions != null) {
            newQuiz.setQuizQuestions(quizQuestions);
        }

        newQuiz.setTitle("Generated Quiz Title");
        unitOfWork.addUpdatedObject(newQuiz);
        return new QuizDto(newQuiz);
    }
}
