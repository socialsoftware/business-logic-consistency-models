package pt.ulisboa.tecnico.socialsoftware.blcm.functionality

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.blcm.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.blcm.SpockTest

import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto
import pt.ulisboa.tecnico.socialsoftware.blcm.question.QuestionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.TopicFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event.TournamentEventDetection
import pt.ulisboa.tecnico.socialsoftware.blcm.user.UserFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler

@DataJpaTest
class CourseExecutionUpdateStudentANDTournamentAddParticipantTest extends SpockTest {
    @Autowired
    private CourseExecutionFunctionalities courseExecutionFunctionalities
    @Autowired
    private UserFunctionalities userFunctionalities
    @Autowired
    private TopicFunctionalities topicFunctionalities
    @Autowired
    private QuestionFunctionalities questionFunctionalities
    @Autowired
    private TournamentFunctionalities tournamentFunctionalities

    @Autowired
    private TournamentEventDetection tournamentEventDetection

    private CourseExecutionDto courseExecutionDto
    private UserDto userDto1, userDto2
    private TopicDto topicDto1, topicDto2
    private QuestionDto questionDto1, questionDto2
    private TournamentDto tournamentDto

    def setup() {
        given: 'a course execution'
        courseExecutionDto = new CourseExecutionDto()
        courseExecutionDto.setName('BLCM')
        courseExecutionDto.setType('TECNICO')
        courseExecutionDto.setAcronym('TESTBLCM')
        courseExecutionDto.setAcademicTerm('2022/2023')
        courseExecutionDto.setEndDate(DateHandler.toISOString(DateHandler.now().plusDays(1)))
        courseExecutionDto = courseExecutionFunctionalities.createCourseExecution(courseExecutionDto)

        userDto1 = new UserDto()
        userDto1.setName('Name' + 1)
        userDto1.setUsername('Username' + 1)
        userDto1.setRole('STUDENT')
        userDto1 = userFunctionalities.createUser(userDto1)

        userFunctionalities.activateUser(userDto1.getAggregateId())

        courseExecutionFunctionalities.addStudent(courseExecutionDto.getAggregateId(), userDto1.getAggregateId())

        userDto2 = new UserDto()
        userDto2.setName('Name' + 2)
        userDto2.setUsername('Username' + 2)
        userDto2.setRole('STUDENT')
        userDto2 = userFunctionalities.createUser(userDto2)

        userFunctionalities.activateUser(userDto2.aggregateId)

        courseExecutionFunctionalities.addStudent(courseExecutionDto.aggregateId, userDto2.aggregateId)

        topicDto1 = new TopicDto()
        topicDto1.setName('Topic' + 1)
        topicDto1 = topicFunctionalities.createTopic(courseExecutionDto.getCourseAggregateId(), topicDto1)
        topicDto2 = new TopicDto()
        topicDto2.setName('Topic' + 2)
        topicDto2 = topicFunctionalities.createTopic(courseExecutionDto.getCourseAggregateId(), topicDto2)

        def questionDto1 = new QuestionDto()
        questionDto1.setTitle('Title' + 1)
        questionDto1.setContent('Content' + 1)
        def set =  new HashSet<>(Arrays.asList(topicDto1));
        questionDto1.setTopicDto(set)
        def optionDto1 = new OptionDto()
        optionDto1.setSequence(1)
        optionDto1.setCorrect(true)
        optionDto1.setContent("Option" + 1)
        def optionDto2 = new OptionDto()
        optionDto2.setSequence(2)
        optionDto2.setCorrect(false)
        optionDto2.setContent("Option" + 2)
        questionDto1.setOptionDtos([optionDto1,optionDto2])
        questionDto1 = questionFunctionalities.createQuestion(courseExecutionDto.getCourseAggregateId(), questionDto1)

        def questionDto2 = new QuestionDto()
        questionDto2.setTitle('Title' + 2)
        questionDto2.setContent('Content' + 2)
        set =  new HashSet<>(Arrays.asList(topicDto2));
        questionDto2.setTopicDto(set)
        questionDto2.setOptionDtos([optionDto1,optionDto2])
        questionDto2 = questionFunctionalities.createQuestion(courseExecutionDto.getCourseAggregateId(), questionDto2)

        tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(DateHandler.toISOString(DateHandler.now().plusHours(1)))
        tournamentDto.setEndTime(DateHandler.toISOString(DateHandler.now().plusHours(2)))
        tournamentDto.setNumberOfQuestions(2)
        tournamentDto = tournamentFunctionalities.createTournament(userDto1.getAggregateId(), courseExecutionDto.getAggregateId(),
                [topicDto1.getAggregateId(),topicDto2.getAggregateId()], tournamentDto)
    }

    def cleanup() {

    }

    def 'sequential update name in course execution and then add student as tournament participant' () {
        given: 'student name is updated'
        def updateNameDto = new UserDto()
        updateNameDto.setName("NewName")
        courseExecutionFunctionalities.updateExecutionStudentName(courseExecutionDto.getAggregateId(), userDto2.getAggregateId(), updateNameDto)

        when: 'student is added to tournament'
        tournamentFunctionalities.addParticipant(tournamentDto.getAggregateId(), userDto2.getAggregateId())

        then: 'the name is updated in course execution'
        def courseExecutionDtoResult = courseExecutionFunctionalities.getCourseExecutionByAggregateId(courseExecutionDto.getAggregateId())
        courseExecutionDtoResult.getStudents().find{it.aggregateId == userDto2.aggregateId}.name == "NewName"
        and: "the name is updated in tournament"
        def tournamentDtoResult = tournamentFunctionalities.findTournament(tournamentDto.getAggregateId())
        tournamentDtoResult.getParticipants().find{it.aggregateId == userDto2.aggregateId}.name == "NewName"
    }

    def 'sequential add student as tournament participant and then update name in course execution' () {
        given: 'student is added to tournament'
        tournamentFunctionalities.addParticipant(tournamentDto.getAggregateId(), userDto2.getAggregateId())
        and: 'student name is updated'
        def updateNameDto = new UserDto()
        updateNameDto.setName("NewName")
        courseExecutionFunctionalities.updateExecutionStudentName(courseExecutionDto.getAggregateId(), userDto2.getAggregateId(), updateNameDto)

        when: 'event event is processed'
        tournamentEventDetection.detectUpdateExecutionStudentNameEvent();

        then: 'the name is updated in course execution'
        def courseExecutionDtoResult = courseExecutionFunctionalities.getCourseExecutionByAggregateId(courseExecutionDto.getAggregateId())
        courseExecutionDtoResult.getStudents().find{it.aggregateId == userDto2.aggregateId}.name == "NewName"
        and: "the name is updated in tournament"
        def tournamentDtoResult = tournamentFunctionalities.findTournament(tournamentDto.getAggregateId())
        tournamentDtoResult.getParticipants().find{it.aggregateId == userDto2.aggregateId}.name == "NewName"
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}