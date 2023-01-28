package pt.ulisboa.tecnico.socialsoftware.blcm.functionality

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.blcm.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.blcm.SpockTest
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto
import pt.ulisboa.tecnico.socialsoftware.blcm.user.UserFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler

@DataJpaTest
class CourseExecutionUpdateStudentANDTournamentAddParticipantTest extends SpockTest {
    @Autowired
    private CourseExecutionFunctionalities courseExecutionFunctionalities
    @Autowired
    private UserFunctionalities userFunctionalities
    private CourseExecution courseExecution

    def setup() {
        given: 'a course execution'
        def courseExecutionDto = new CourseExecutionDto()
        courseExecutionDto.setName('BLCM')
        courseExecutionDto.setType('TECNICO')
        courseExecutionDto.setAcronym('TESTBLCM')
        courseExecutionDto.setAcademicTerm('2022/2023')
        courseExecutionDto.setEndDate(DateHandler.toISOString(DateHandler.now().plusDays(1)))
        courseExecutionDto = courseExecutionFunctionalities.createCourseExecution(courseExecutionDto)
    }

    def cleanup() {

    }

    def 'update first and add next' () {
        given:
        def userDto = new UserDto()
        userDto.setName('Name')
        userDto.setUsername('Username')
        userDto.setRole('STUDENT')

        when:
        def result = userFunctionalities.createUser(userDto)

        then:
        result.getName() == 'Name'
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}