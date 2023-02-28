package pt.ulisboa.tecnico.socialsoftware.blcm

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.QuizAnswerFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.event.QuizAnswerEventHandling
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.event.QuizAnswerEventProcessing
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.service.QuizAnswerService
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.VersionService
import pt.ulisboa.tecnico.socialsoftware.blcm.config.StartUpService
import pt.ulisboa.tecnico.socialsoftware.blcm.course.service.CourseService
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.CourseExecutionEventHandling
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.CourseExecutionEventProcessing
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService
import pt.ulisboa.tecnico.socialsoftware.blcm.question.QuestionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.question.event.QuestionEventHandling
import pt.ulisboa.tecnico.socialsoftware.blcm.question.event.QuestionEventProcessing
import pt.ulisboa.tecnico.socialsoftware.blcm.question.service.QuestionService
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.event.QuizEventHandling
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.event.QuizEventProcessing
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.TopicFunctionalities

import pt.ulisboa.tecnico.socialsoftware.blcm.topic.service.TopicService
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event.TournamentEventHandling
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event.TournamentEventProcessing
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService
import pt.ulisboa.tecnico.socialsoftware.blcm.user.UserFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.user.service.UserService

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