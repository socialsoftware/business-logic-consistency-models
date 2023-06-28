package pt.ulisboa.tecnico.socialsoftware.ms

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import pt.ulisboa.tecnico.socialsoftware.ms.answer.QuizAnswerFunctionalities
import pt.ulisboa.tecnico.socialsoftware.ms.answer.event.QuizAnswerEventHandling
import pt.ulisboa.tecnico.socialsoftware.ms.answer.event.QuizAnswerEventProcessing
import pt.ulisboa.tecnico.socialsoftware.ms.answer.service.QuizAnswerService
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.aggregate.service.AggregateIdGeneratorService

import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.version.VersionService
import pt.ulisboa.tecnico.socialsoftware.ms.config.StartUpService
import pt.ulisboa.tecnico.socialsoftware.ms.course.service.CourseService
import pt.ulisboa.tecnico.socialsoftware.ms.execution.CourseExecutionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.CourseExecutionEventHandling
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.CourseExecutionEventProcessing
import pt.ulisboa.tecnico.socialsoftware.ms.execution.service.CourseExecutionService
import pt.ulisboa.tecnico.socialsoftware.ms.question.QuestionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.ms.question.event.QuestionEventHandling
import pt.ulisboa.tecnico.socialsoftware.ms.question.event.QuestionEventProcessingEvent
import pt.ulisboa.tecnico.socialsoftware.ms.question.service.QuestionService
import pt.ulisboa.tecnico.socialsoftware.ms.quiz.QuizFunctionalities
import pt.ulisboa.tecnico.socialsoftware.ms.quiz.event.QuizEventHandling
import pt.ulisboa.tecnico.socialsoftware.ms.quiz.event.QuizEventProcessing
import pt.ulisboa.tecnico.socialsoftware.ms.quiz.service.QuizService
import pt.ulisboa.tecnico.socialsoftware.ms.topic.TopicFunctionalities

import pt.ulisboa.tecnico.socialsoftware.ms.topic.service.TopicService
import pt.ulisboa.tecnico.socialsoftware.ms.tournament.TournamentFunctionalities
import pt.ulisboa.tecnico.socialsoftware.ms.tournament.event.TournamentEventHandling
import pt.ulisboa.tecnico.socialsoftware.ms.tournament.event.TournamentEventProcessing
import pt.ulisboa.tecnico.socialsoftware.ms.tournament.service.TournamentService
import pt.ulisboa.tecnico.socialsoftware.ms.user.UserFunctionalities
import pt.ulisboa.tecnico.socialsoftware.ms.user.service.UserService

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
    QuestionEventProcessingEvent questionEventProcessing() {
        return new QuestionEventProcessingEvent();
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