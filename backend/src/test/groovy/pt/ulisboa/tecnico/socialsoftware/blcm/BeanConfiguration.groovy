package pt.ulisboa.tecnico.socialsoftware.blcm

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.AnswerFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.event.AnswerEventDetection
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.service.AnswerService
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.service.VersionService
import pt.ulisboa.tecnico.socialsoftware.blcm.config.StartUpService
import pt.ulisboa.tecnico.socialsoftware.blcm.course.service.CourseService
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.CourseExecutionEventDetection
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService
import pt.ulisboa.tecnico.socialsoftware.blcm.question.QuestionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.question.event.QuestionEventDetection
import pt.ulisboa.tecnico.socialsoftware.blcm.question.service.QuestionService
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.event.QuizEventDetection
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.service.QuizService
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.TopicFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.event.TopicEventDetection
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.service.TopicService
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event.TournamentEventDetection
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
    QuizFunctionalities quizFunctionalities() {
        return new QuizFunctionalities()
    }

    @Bean
    AnswerFunctionalities answerFunctionalities() {
        return new AnswerFunctionalities()
    }

    @Bean
    TournamentFunctionalities tournamentFunctionalities() {
        return new TournamentFunctionalities()
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
    AnswerService answerService() {
        return new AnswerService()
    }

    @Bean
    TournamentService tournamentService() {
        return new TournamentService()
    }

    @Bean
    CourseExecutionEventDetection courseExecutionEventDetection() {
        return new CourseExecutionEventDetection()
    }

    @Bean
    TopicEventDetection topicEventDetection() {
        return new TopicEventDetection()
    }

    @Bean
    QuestionEventDetection questionEventDetection() {
        return new QuestionEventDetection()
    }

    @Bean
    QuizEventDetection quizEventDetection() {
        return new QuizEventDetection()
    }

    @Bean
    AnswerEventDetection answerEventDetection() {
        return new AnswerEventDetection()
    }

    @Bean
    TournamentEventDetection tournamentEventDetection() {
        return new TournamentEventDetection()
    }
}