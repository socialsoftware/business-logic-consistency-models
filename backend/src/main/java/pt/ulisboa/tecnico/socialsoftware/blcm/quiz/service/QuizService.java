package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.InvalidateQuizEvent;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.INACTIVE;
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
    private EventRepository eventRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CourseExecutionService courseExecutionService;


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

        if(quiz.getState() == DELETED) {
            throw new TutorException(QUIZ_DELETED, quiz.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();
        unitOfWork.addToCausalSnapshot(quiz, allEvents);
        return quiz;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizDto generateQuiz(Integer courseExecutionAggregateId, QuizDto quizDto, List<Integer> topicIds, Integer numberOfQuestions, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();

        QuizCourseExecution quizCourseExecution = new QuizCourseExecution(courseExecutionService.getCausalCourseExecutionRemote(courseExecutionAggregateId, unitOfWork));

        List<QuestionDto> questionDtos = questionService.findQuestionsByTopics(topicIds, unitOfWork);

        if(questionDtos.size() < numberOfQuestions) {
            throw new TutorException(ErrorMessage.NOT_ENOUGH_QUESTIONS);
        }

        Set<Integer> questionPositions = new HashSet<>();
        while(questionPositions.size() < numberOfQuestions) {
            questionPositions.add(ThreadLocalRandom.current().nextInt(0, questionDtos.size()));
        }

        Set<QuizQuestion> quizQuestions = questionPositions.stream()
                .map(pos -> questionDtos.get(pos))
                .map(QuizQuestion::new)
                .collect(Collectors.toSet());


        Quiz quiz = new Quiz(aggregateId, quizCourseExecution, quizQuestions, quizDto, GENERATED);
        quiz.setTitle("Generated Quiz Title");
        unitOfWork.registerChanged(quiz);
        return new QuizDto(quiz);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizDto startTournamentQuiz(Integer userAggregateId, Integer quizAggregateId, UnitOfWork unitOfWork) {
        /* must add more verifications */
        Quiz oldQuiz = getCausalQuizLocal(quizAggregateId, unitOfWork);
        QuizDto quizDto = new QuizDto(oldQuiz);
        List<QuestionDto> questionDtoList = new ArrayList<>();
        // TODO if I have time change the quiz to only store references to the questions (its easier)
        oldQuiz.getQuizQuestions().forEach(qq -> {
            QuestionDto questionDto = questionService.getCausalQuestionRemote(qq.getAggregateId(), unitOfWork);
            questionDto.getOptionDtos().forEach(o -> {
                o.setCorrect(false); // by setting all to false frontend doesn't know which is correct
            });
            questionDtoList.add(questionDto);
        });
        quizDto.setQuestionDtos(questionDtoList);
        return quizDto;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizDto createQuiz(QuizCourseExecution quizCourseExecution, Set<QuizQuestion> quizQuestions, QuizDto quizDto, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        Quiz quiz = new Quiz(aggregateId, quizCourseExecution, quizQuestions, quizDto, IN_CLASS);
        unitOfWork.registerChanged(quiz);
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

        if (topicsAggregateIds != null && numberOfQuestions != null) {
            List<QuestionDto> questionDtos = questionService.findQuestionsByTopics(new ArrayList<>(topicsAggregateIds), unitOfWork);

            if (questionDtos.size() < numberOfQuestions) {
                throw new TutorException(ErrorMessage.NOT_ENOUGH_QUESTIONS);
            }

            Set<QuizQuestion> quizQuestions = questionDtos.stream()
                    .map(QuizQuestion::new)
                    .collect(Collectors.toSet());

            if (quizQuestions != null) {
                newQuiz.setQuizQuestions(quizQuestions);
            }
        }

        newQuiz.setTitle("Generated Quiz Title");
        unitOfWork.registerChanged(newQuiz);
        return new QuizDto(newQuiz);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizDto updateQuiz(QuizDto quizDto, Set<QuizQuestion> quizQuestions, UnitOfWork unitOfWork) {
        Quiz oldQuiz = getCausalQuizLocal(quizDto.getAggregateId(), unitOfWork);
        Quiz newQuiz = new Quiz(oldQuiz);


        if (quizDto.getTitle() != null) {
            newQuiz.setTitle(quizDto.getTitle());
            unitOfWork.registerChanged(newQuiz);
        }

        if(quizDto.getAvailableDate() != null) {
            newQuiz.setAvailableDate(LocalDateTime.parse(quizDto.getAvailableDate()));
        }

        if(quizDto.getConclusionDate() != null) {
            newQuiz.setConclusionDate(LocalDateTime.parse(quizDto.getConclusionDate()));
        }

        if(quizDto.getResultsDate() != null) {
            newQuiz.setResultsDate(LocalDateTime.parse(quizDto.getResultsDate()));
        }

        if(quizQuestions != null && quizQuestions.size() > 0) {
            newQuiz.setQuizQuestions(quizQuestions);
        }

        return new QuizDto(newQuiz);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<QuizDto> getAvailableQuizzes(Integer courseExecutionAggregateId, UnitOfWork unitOfWork) {
        LocalDateTime now = LocalDateTime.now();
       return quizRepository.findAllAggregateIdsByCourseExecution(courseExecutionAggregateId).stream()
               .map(id -> getCausalQuizLocal(id, unitOfWork))
               .filter(quiz -> quiz.getAvailableDate().isAfter(now) && quiz.getConclusionDate().isBefore(now) && quiz.getQuizType() != GENERATED)
               .map(QuizDto::new)
               .collect(Collectors.toList());

    }

    /************************************************ EVENT PROCESSING ************************************************/

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Quiz removeCourseExecution(Integer quizAggregateId, Integer courseExecutionId, Integer aggregateVersion, UnitOfWork unitOfWork) {
        Quiz oldQuiz = getCausalQuizLocal(quizAggregateId, unitOfWork);
        Quiz newQuiz = new Quiz(oldQuiz);
        
        if(newQuiz.getCourseExecution().getAggregateId().equals(courseExecutionId)) {
            newQuiz.setState(INACTIVE);
            unitOfWork.registerChanged(newQuiz);
            return newQuiz;
        }
        
        return null;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Quiz updateQuestion(Integer quizAggregateId, Integer questionAggregateId, String title, String content, Integer aggregateVersion, UnitOfWork unitOfWork) {
        Quiz oldQuiz = getCausalQuizLocal(quizAggregateId, unitOfWork);
        Quiz newQuiz = new Quiz(oldQuiz);

        QuizQuestion quizQuestion = newQuiz.findQuestion(questionAggregateId);

        if (quizQuestion == null) {
            return null;
        }

        quizQuestion.setTitle(title);
        quizQuestion.setContent(content);
        quizQuestion.setVersion(aggregateVersion);

        return newQuiz;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Quiz removeQuestion(Integer quizAggregateId, Integer questionAggregateId, Integer aggregateVersion, UnitOfWork unitOfWork) {
        Quiz oldQuiz = getCausalQuizLocal(quizAggregateId, unitOfWork);
        Quiz newQuiz = new Quiz(oldQuiz);

        QuizQuestion quizQuestion = newQuiz.findQuestion(questionAggregateId);

        if (quizQuestion == null) {
            return null;
        }

        newQuiz.setState(INACTIVE);
        quizQuestion.setState(DELETED);
        unitOfWork.addEvent(new InvalidateQuizEvent(newQuiz.getAggregateId()));
        return newQuiz;
    }
}
