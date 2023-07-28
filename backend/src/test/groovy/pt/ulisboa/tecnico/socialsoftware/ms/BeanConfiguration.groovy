package pt.ulisboa.tecnico.socialsoftware.ms

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventService
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.service.CausalConsistencyService
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.QuizAnswerFunctionalities
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.event.QuizAnswerEventHandling
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.event.QuizAnswerEventProcessing
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.answer.service.QuizAnswerService
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService

import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.version.VersionService
import pt.ulisboa.tecnico.socialsoftware.ms.config.StartUpService
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.service.CourseService
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.CourseExecutionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.event.CourseExecutionEventHandling
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.event.CourseExecutionEventProcessing
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.service.CourseExecutionService
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.QuestionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.event.QuestionEventHandling
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.event.QuestionEventProcessing
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.service.QuestionService
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.QuizFunctionalities
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.event.QuizEventHandling
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.event.QuizEventProcessing
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.service.QuizService
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.TopicFunctionalities

import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.service.TopicService
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.TournamentFunctionalities
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.event.TournamentEventHandling
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.event.TournamentEventProcessing
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.service.TournamentService
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.UserFunctionalities
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.service.UserService

@TestConfiguration
@PropertySource("classpath:application-test.properties")
class BeanConfiguration {
    @Bean
    StartUpService startUpService() {
        return new StartUpService();
    }

    @Bean
    AggregateIdGeneratorService aggregateIdGeneratorService() {
        return new AggregateIdGeneratorService();
    }

    @Bean
    VersionService versionService() {
        return new VersionService();
    }

    @Bean
    EventService eventService() {
        return new EventService();
    }

    @Bean
    UnitOfWorkService unitOfWorkService() {
        return new UnitOfWorkService();
    }

    @Bean
    CourseExecutionFunctionalities courseExecutionFunctionalities() {
        return new CourseExecutionFunctionalities()
    }

    @Bean
    CourseExecutionEventProcessing courseExecutionEventProcessing() {
        return new CourseExecutionEventProcessing()
    }

    @Bean
    UserFunctionalities userFunctionalities() {
        return new UserFunctionalities()
    }

    @Bean
    TopicFunctionalities topicFunctionalities() {
        return new TopicFunctionalities()
    }

    @Bean
    QuestionFunctionalities questionFunctionalities() {
        return new QuestionFunctionalities()
    }

    @Bean
    QuestionEventProcessing questionEventProcessing() {
        return new QuestionEventProcessing();
    }

    @Bean
    QuizFunctionalities quizFunctionalities() {
        return new QuizFunctionalities()
    }

    @Bean
    QuizEventProcessing quizEventProcessing() {
        return new QuizEventProcessing();
    }

    @Bean
    QuizAnswerFunctionalities answerFunctionalities() {
        return new QuizAnswerFunctionalities()
    }

    @Bean
    QuizAnswerEventProcessing answerEventProcessing() {
        return new QuizAnswerEventProcessing()
    }

    @Bean
    TournamentFunctionalities tournamentFunctionalities() {
        return new TournamentFunctionalities()
    }

    @Bean
    TournamentEventProcessing tournamentEventProcessing() {
        return new TournamentEventProcessing()
    }

    @Bean
    CausalConsistencyService aggregateService() {
        return new CausalConsistencyService();
    }

    @Bean
    CourseService courseService() {
        return new CourseService()
    }

    @Bean
    CourseExecutionService courseExecutionService() {
        return new CourseExecutionService()
    }

    @Bean
    UserService userService() {
        return new UserService()
    }

    @Bean
    TopicService topicService() {
        return new TopicService()
    }

    @Bean
    QuestionService questionService() {
        return new QuestionService()
    }

    @Bean
    QuizService quizService() {
        return new QuizService()
    }

    @Bean
    QuizAnswerService answerService() {
        return new QuizAnswerService()
    }

    @Bean
    TournamentService tournamentService() {
        return new TournamentService()
    }

    @Bean
    CourseExecutionEventHandling courseExecutionEventDetection() {
        return new CourseExecutionEventHandling()
    }

    @Bean
    QuestionEventHandling questionEventDetection() {
        return new QuestionEventHandling()
    }

    @Bean
    QuizEventHandling quizEventDetection() {
        return new QuizEventHandling()
    }

    @Bean
    QuizAnswerEventHandling answerEventDetection() {
        return new QuizAnswerEventHandling()
    }

    @Bean
    TournamentEventHandling tournamentEventDetection() {
        return new TournamentEventHandling()
    }
}