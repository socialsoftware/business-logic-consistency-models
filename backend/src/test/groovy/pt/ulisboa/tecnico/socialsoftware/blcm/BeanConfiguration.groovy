package pt.ulisboa.tecnico.socialsoftware.blcm

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.service.VersionService
import pt.ulisboa.tecnico.socialsoftware.blcm.config.StartUpService
import pt.ulisboa.tecnico.socialsoftware.blcm.course.service.CourseService
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService
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
}