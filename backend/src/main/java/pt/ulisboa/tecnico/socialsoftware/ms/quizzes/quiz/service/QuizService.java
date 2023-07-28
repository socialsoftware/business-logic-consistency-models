package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.repository.CausalConsistencyRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.service.CausalConsistencyService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.tcc.QuizTCC;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.tcc.QuizTCCRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.event.publish.InvalidateQuizEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.QuizCourseExecution;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.dto.QuizDto;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.QuizType.GENERATED;
import static pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.QuizType.IN_CLASS;

@Service
public class QuizService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private CausalConsistencyService causalConsistencyService;

    @Autowired
    private QuizTCCRepository quizTCCRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CourseExecutionService courseExecutionService;

    @Autowired
    private CausalConsistencyRepository causalConsistencyRepository;


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizDto addQuizCausalSnapshot(Integer aggregateId, UnitOfWork unitOfWork) {
        return new QuizDto((QuizTCC) causalConsistencyService.addAggregateCausalSnapshot(aggregateId, unitOfWork));
    }

    // intended for requests from local functionalities

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizDto generateQuiz(Integer courseExecutionAggregateId, QuizDto quizDto, List<Integer> topicIds, Integer numberOfQuestions, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();

        QuizCourseExecution quizCourseExecution = new QuizCourseExecution(courseExecutionService.addCourseExecutionCausalSnapshot(courseExecutionAggregateId, unitOfWork));

        List<QuestionDto> questionDtos = questionService.findQuestionsByTopics(topicIds, unitOfWork);

        if (questionDtos.size() < numberOfQuestions) {
            throw new TutorException(ErrorMessage.NOT_ENOUGH_QUESTIONS);
        }

        Set<Integer> questionPositions = new HashSet<>();
        while (questionPositions.size() < numberOfQuestions) {
            questionPositions.add(ThreadLocalRandom.current().nextInt(0, questionDtos.size()));
        }

        Set<QuizQuestion> quizQuestions = questionPositions.stream()
                .map(pos -> questionDtos.get(pos))
                .map(QuizQuestion::new)
                .collect(Collectors.toSet());


        QuizTCC quiz = new QuizTCC(aggregateId, quizCourseExecution, quizQuestions, quizDto, GENERATED);
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
        Quiz oldQuiz = (QuizTCC) causalConsistencyService.addAggregateCausalSnapshot(quizAggregateId, unitOfWork);
        QuizDto quizDto = new QuizDto(oldQuiz);
        List<QuestionDto> questionDtoList = new ArrayList<>();
        // TODO if I have time change the quiz to only store references to the questions (its easier)
        oldQuiz.getQuizQuestions().forEach(qq -> {
            QuestionDto questionDto = questionService.addQuestionCausalSnapshot(qq.getQuestionAggregateId(), unitOfWork);
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
    public QuizDto createQuiz(QuizCourseExecution quizCourseExecution, Set<QuestionDto> questions, QuizDto quizDto, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();

        Set<QuizQuestion> quizQuestions = questions.stream()
                .map(QuizQuestion::new)
                .collect(Collectors.toSet());

        QuizTCC quiz = new QuizTCC(aggregateId, quizCourseExecution, quizQuestions, quizDto, IN_CLASS);
        unitOfWork.registerChanged(quiz);
        return new QuizDto(quiz);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizDto updateGeneratedQuiz(QuizDto quizDto, Set<Integer> topicsAggregateIds, Integer numberOfQuestions, UnitOfWork unitOfWork) {
        QuizTCC oldQuiz = (QuizTCC) causalConsistencyService.addAggregateCausalSnapshot(quizDto.getAggregateId(), unitOfWork);
        QuizTCC newQuiz = new QuizTCC(oldQuiz);
        newQuiz.update(quizDto);

        if (topicsAggregateIds != null && numberOfQuestions != null) {
            List<QuestionDto> questionDtos = questionService.findQuestionsByTopics(new ArrayList<>(topicsAggregateIds), unitOfWork);

            if (questionDtos.size() < numberOfQuestions) {
                throw new TutorException(ErrorMessage.NOT_ENOUGH_QUESTIONS);
            }

            Set<QuizQuestion> quizQuestions = questionDtos.stream()
                    .map(QuizQuestion::new)
                    .collect(Collectors.toSet());

            newQuiz.setQuizQuestions(quizQuestions);
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
        QuizTCC oldQuiz = (QuizTCC) causalConsistencyService.addAggregateCausalSnapshot(quizDto.getAggregateId(), unitOfWork);
        QuizTCC newQuiz = new QuizTCC(oldQuiz);


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
       return quizTCCRepository.findAllQuizIdsByCourseExecution(courseExecutionAggregateId).stream()
               .map(id -> (QuizTCC) causalConsistencyService.addAggregateCausalSnapshot(id, unitOfWork))
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
        QuizTCC oldQuiz = (QuizTCC) causalConsistencyService.addAggregateCausalSnapshot(quizAggregateId, unitOfWork);
        QuizTCC newQuiz = new QuizTCC(oldQuiz);
        
        if (newQuiz.getQuizCourseExecution().getCourseExecutionAggregateId().equals(courseExecutionId)) {
            newQuiz.setState(Aggregate.AggregateState.INACTIVE);
            unitOfWork.registerChanged(newQuiz);
            return newQuiz;
        }
        
        return null;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateQuestion(Integer quizAggregateId, Integer questionAggregateId, String title, String content, Integer aggregateVersion, UnitOfWork unitOfWork) {
        QuizTCC oldQuiz = (QuizTCC) causalConsistencyService.addAggregateCausalSnapshot(quizAggregateId, unitOfWork);
        QuizTCC newQuiz = new QuizTCC(oldQuiz);

        QuizQuestion quizQuestion = newQuiz.findQuestion(questionAggregateId);

        if (quizQuestion != null) {
            quizQuestion.setTitle(title);
            quizQuestion.setContent(content);
            quizQuestion.setQuestionVersion(aggregateVersion);
        }
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeQuizQuestion(Integer quizAggregateId, Integer questionAggregateId, UnitOfWork unitOfWork) {
        QuizTCC oldQuiz = (QuizTCC) causalConsistencyService.addAggregateCausalSnapshot(quizAggregateId, unitOfWork);
        QuizTCC newQuiz = new QuizTCC(oldQuiz);

        QuizQuestion quizQuestion = newQuiz.findQuestion(questionAggregateId);

        if (quizQuestion != null) {
            newQuiz.setState(Aggregate.AggregateState.INACTIVE);
            quizQuestion.setState(Aggregate.AggregateState.DELETED);
            unitOfWork.addEvent(new InvalidateQuizEvent(newQuiz.getAggregateId()));
        }
    }
}
