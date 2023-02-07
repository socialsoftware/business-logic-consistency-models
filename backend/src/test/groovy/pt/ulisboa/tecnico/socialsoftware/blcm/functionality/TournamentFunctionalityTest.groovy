package pt.ulisboa.tecnico.socialsoftware.blcm.functionality

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.blcm.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.blcm.SpockTest
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.service.VersionService
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto
import pt.ulisboa.tecnico.socialsoftware.blcm.question.QuestionFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.TopicFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event.TournamentEventDetection
import pt.ulisboa.tecnico.socialsoftware.blcm.user.UserFunctionalities
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler

@DataJpaTest
class TournamentFunctionalityTest extends SpockTest {
    public static final String UPDATED_NAME = "UpdatedName"
    public static final String ANONYMOUS = 'ANONYMOUS'

    @Autowired
    private CourseExecutionFunctionalities courseExecutionFunctionalities
    @Autowired
    private UserFunctionalities userFunctionalities
    @Autowired
    private TopicFunctionalities topicFunctionalities
    @Autowired
    private QuestionFunctionalities questionFunctionalities
    @Autowired
    private QuizFunctionalities quizFunctionalities
    @Autowired
    private TournamentFunctionalities tournamentFunctionalities

    @Autowired
    private VersionService versionService;

    @Autowired
    private TournamentEventDetection tournamentEventDetection

    private CourseExecutionDto courseExecutionDto
    private UserDto userDto1, userDto2
    private TopicDto topicDto1, topicDto2, topicDto3
    private QuestionDto questionDto1, questionDto2, questionDto3
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
        topicDto3 = new TopicDto()
        topicDto3.setName('Topic' + 3)
        topicDto3 = topicFunctionalities.createTopic(courseExecutionDto.getCourseAggregateId(), topicDto3)

        questionDto1 = new QuestionDto()
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

        questionDto2 = new QuestionDto()
        questionDto2.setTitle('Title' + 2)
        questionDto2.setContent('Content' + 2)
        set =  new HashSet<>(Arrays.asList(topicDto2));
        questionDto2.setTopicDto(set)
        questionDto2.setOptionDtos([optionDto1,optionDto2])
        questionDto2 = questionFunctionalities.createQuestion(courseExecutionDto.getCourseAggregateId(), questionDto2)

        questionDto3 = new QuestionDto()
        questionDto3.setTitle('Title' + 2)
        questionDto3.setContent('Content' + 2)
        set =  new HashSet<>(Arrays.asList(topicDto3));
        questionDto3.setTopicDto(set)
        questionDto3.setOptionDtos([optionDto1,optionDto2])
        questionDto3 = questionFunctionalities.createQuestion(courseExecutionDto.getCourseAggregateId(), questionDto3)

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
        updateNameDto.setName(UPDATED_NAME)
        courseExecutionFunctionalities.updateExecutionStudentName(courseExecutionDto.getAggregateId(), userDto2.getAggregateId(), updateNameDto)

        when: 'student is added to tournament'
        tournamentFunctionalities.addParticipant(tournamentDto.getAggregateId(), userDto2.getAggregateId())

        then: 'the name is updated in course execution'
        def courseExecutionDtoResult = courseExecutionFunctionalities.getCourseExecutionByAggregateId(courseExecutionDto.getAggregateId())
        courseExecutionDtoResult.getStudents().find{it.aggregateId == userDto2.aggregateId}.name == UPDATED_NAME
        and: 'the name is updated in tournament'
        def tournamentDtoResult = tournamentFunctionalities.findTournament(tournamentDto.getAggregateId())
        tournamentDtoResult.getParticipants().find{it.aggregateId == userDto2.aggregateId}.name == UPDATED_NAME
    }

    def 'sequential add student as tournament participant and then update name in course execution' () {
        given: 'student is added to tournament'
        tournamentFunctionalities.addParticipant(tournamentDto.getAggregateId(), userDto2.getAggregateId())
        and: 'student name is updated'
        def updateNameDto = new UserDto()
        updateNameDto.setName(UPDATED_NAME)
        courseExecutionFunctionalities.updateExecutionStudentName(courseExecutionDto.getAggregateId(), userDto2.getAggregateId(), updateNameDto)

        when: 'update name event is processed'
        tournamentEventDetection.detectUpdateExecutionStudentNameEvent();

        then: 'the name is updated in course execution'
        def courseExecutionDtoResult = courseExecutionFunctionalities.getCourseExecutionByAggregateId(courseExecutionDto.getAggregateId())
        courseExecutionDtoResult.getStudents().find{it.aggregateId == userDto2.aggregateId}.name == UPDATED_NAME
        and: 'the name is updated in tournament'
        def tournamentDtoResult = tournamentFunctionalities.findTournament(tournamentDto.getAggregateId())
        tournamentDtoResult.getParticipants().find{it.aggregateId == userDto2.aggregateId}.name == UPDATED_NAME
    }

    def 'concurrent add student as tournament participant and update name in course execution: add student finishes first' () {
        given: 'student is added to tournament'
        tournamentFunctionalities.addParticipant(tournamentDto.getAggregateId(), userDto2.getAggregateId())
        and: 'the version number is decreased to simulate concurrency'
        versionService.decrementVersionNumber()
        and: 'student name is updated'
        def updateNameDto = new UserDto()
        updateNameDto.setName(UPDATED_NAME)
        courseExecutionFunctionalities.updateExecutionStudentName(courseExecutionDto.getAggregateId(), userDto2.getAggregateId(), updateNameDto)
        and: 'reset version number'
        versionService.incrementAndGetVersionNumber()

        when: 'update name event is processed'
        tournamentEventDetection.detectUpdateExecutionStudentNameEvent();

        then: 'the name is updated in course execution'
        def courseExecutionDtoResult = courseExecutionFunctionalities.getCourseExecutionByAggregateId(courseExecutionDto.getAggregateId())
        courseExecutionDtoResult.getStudents().find{it.aggregateId == userDto2.aggregateId}.name == UPDATED_NAME
        and: 'the name is updated in tournament'
        def tournamentDtoResult = tournamentFunctionalities.findTournament(tournamentDto.getAggregateId())
        tournamentDtoResult.getParticipants().find{it.aggregateId == userDto2.aggregateId}.name == UPDATED_NAME
    }

    def 'concurrent add student as tournament participant and update name in course execution: update name finishes first' () {
        given: 'student name is updated'
        def updateNameDto = new UserDto()
        updateNameDto.setName(UPDATED_NAME)
        courseExecutionFunctionalities.updateExecutionStudentName(courseExecutionDto.getAggregateId(), userDto2.getAggregateId(), updateNameDto)
        and: 'try to process update name event'
        tournamentEventDetection.detectUpdateExecutionStudentNameEvent();
        and: 'the version number is decreased to simulate concurrency'
        versionService.decrementVersionNumber()
        and: 'student is added to tournament'
        tournamentFunctionalities.addParticipant(tournamentDto.getAggregateId(), userDto2.getAggregateId())
        and: 'reset version number'
        versionService.incrementAndGetVersionNumber()

        when: 'update name event is processed'
        tournamentEventDetection.detectUpdateExecutionStudentNameEvent();

        then: 'the name is updated in course execution'
        def courseExecutionDtoResult = courseExecutionFunctionalities.getCourseExecutionByAggregateId(courseExecutionDto.getAggregateId())
        courseExecutionDtoResult.getStudents().find{it.aggregateId == userDto2.aggregateId}.name == UPDATED_NAME
        and: 'the name is updated in tournament'
        def tournamentDtoResult = tournamentFunctionalities.findTournament(tournamentDto.getAggregateId())
        tournamentDtoResult.getParticipants().find{it.aggregateId == userDto2.aggregateId}.name == UPDATED_NAME
    }

    def 'concurrent add creator as tournament participant and update name in course execution: update name finishes first but cannot get a causal snapshot' () {
        given: 'creator name is updated'
        def updateNameDto = new UserDto()
        updateNameDto.setName(UPDATED_NAME)
        courseExecutionFunctionalities.updateExecutionStudentName(courseExecutionDto.getAggregateId(), userDto1.getAggregateId(), updateNameDto)
        and: 'process update name event'
        tournamentEventDetection.detectUpdateExecutionStudentNameEvent();
        and: 'the version number is decreased to simulate concurrency'
        versionService.decrementVersionNumber()

        when: 'trying to add creator as participant'
        tournamentFunctionalities.addParticipant(tournamentDto.getAggregateId(), userDto1.getAggregateId())

        then: 'fails because the event was processed in tournament but not in the older course execution version'
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.CANNOT_PERFORM_CAUSAL_READ
        and: 'reset version number'
        versionService.incrementAndGetVersionNumber()
        and: 'the name is updated in course execution'
        def courseExecutionDtoResult = courseExecutionFunctionalities.getCourseExecutionByAggregateId(courseExecutionDto.getAggregateId())
        courseExecutionDtoResult.getStudents().find{it.aggregateId == userDto1.aggregateId}.name == UPDATED_NAME
        and: 'the creator is update in tournament'
        def tournamentDtoResult = tournamentFunctionalities.findTournament(tournamentDto.getAggregateId())
        tournamentDtoResult.creator.name == UPDATED_NAME
        and: 'there is no participants'
        tournamentDtoResult.getParticipants().size() == 0
    }

    def 'concurrent add creator as tournament participant and update name in course execution: update name finishes first but breaks invariants' () {
        given: 'creator name is updated'
        def updateNameDto = new UserDto()
        updateNameDto.setName(UPDATED_NAME)
        courseExecutionFunctionalities.updateExecutionStudentName(courseExecutionDto.getAggregateId(), userDto1.getAggregateId(), updateNameDto)
        and: 'the version number is decreased to simulate concurrency'
        versionService.decrementVersionNumber()
        and: 'add creator as participant'
        tournamentFunctionalities.addParticipant(tournamentDto.getAggregateId(), userDto1.getAggregateId())
        and: 'the version number is decreased to simulate concurrency'
        versionService.decrementVersionNumber()

        when: 'process update name in the tournament that does not have participant and' +
                'when merging with the tournament that has participant, creator and participant have different names'
        tournamentEventDetection.detectUpdateExecutionStudentNameEvent()

        then: 'fails because invariant about same info for creator as participant breaks'
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.INVARIANT_BREAK
        and: 'reset version number'
        versionService.incrementAndGetVersionNumber()
        and: 'process update name event'
        tournamentEventDetection.detectUpdateExecutionStudentNameEvent();
        and: 'the name is updated in course execution'
        def courseExecutionDtoResult = courseExecutionFunctionalities.getCourseExecutionByAggregateId(courseExecutionDto.getAggregateId())
        courseExecutionDtoResult.getStudents().find{it.aggregateId == userDto1.aggregateId}.name == UPDATED_NAME
        and: 'the creator is update in tournament'
        def tournamentDtoResult = tournamentFunctionalities.findTournament(tournamentDto.getAggregateId())
        tournamentDtoResult.creator.name == UPDATED_NAME
        and: 'the creator is participant with updated name'
        tournamentDtoResult.getParticipants().size() == 1
        tournamentDtoResult.getParticipants().find{it.aggregateId == userDto1.aggregateId}.name == UPDATED_NAME
    }

    def 'sequential update name in course execution and add creator as tournament participant: fails because when creator is added tournament did not processed the event yet' () {
        given: 'creator name is updated'
        def updateNameDto = new UserDto()
        updateNameDto.setName(UPDATED_NAME)
        courseExecutionFunctionalities.updateExecutionStudentName(courseExecutionDto.getAggregateId(), userDto1.getAggregateId(), updateNameDto)

        when: 'add creator as participant'
        tournamentFunctionalities.addParticipant(tournamentDto.getAggregateId(), userDto1.getAggregateId())

        then: 'fails because update event was emitted but not consumed by tournament'
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.CANNOT_PERFORM_CAUSAL_READ
        and: 'when event is finally processed'
        tournamentEventDetection.detectUpdateExecutionStudentNameEvent()
        and: 'creator can be added as participant'
        tournamentFunctionalities.addParticipant(tournamentDto.getAggregateId(), userDto1.getAggregateId())
        and: 'the name is updated in course execution'
        def courseExecutionDtoResult = courseExecutionFunctionalities.getCourseExecutionByAggregateId(courseExecutionDto.getAggregateId())
        courseExecutionDtoResult.getStudents().find{it.aggregateId == userDto1.aggregateId}.name == UPDATED_NAME
        and: 'the creator is update in tournament'
        def tournamentDtoResult = tournamentFunctionalities.findTournament(tournamentDto.getAggregateId())
        tournamentDtoResult.creator.name == UPDATED_NAME
        and: 'the creator is participant with updated name'
        tournamentDtoResult.getParticipants().size() == 1
        tournamentDtoResult.getParticipants().find{it.aggregateId == userDto1.aggregateId}.name == UPDATED_NAME
    }

    def 'concurrent anonymize creator and add student' () {
        given: 'anonymize creator'
        courseExecutionFunctionalities.anonymizeStudent(courseExecutionDto.aggregateId, userDto1.aggregateId)
        and: 'tournament process event'
        tournamentEventDetection.detectAnonymizeStudentEvents()
        and: 'the version number is decreased to simulate concurrency'
        versionService.decrementVersionNumber()

        when: 'another student is concurrently added to the tournament'
        tournamentFunctionalities.addParticipant(tournamentDto.getAggregateId(), userDto2.getAggregateId())

        then: 'fails during merge because an event was emitted that was not processed by the tournament version'
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.CANNOT_PERFORM_CAUSAL_READ
        and: 'after reset version number'
        versionService.incrementAndGetVersionNumber()
        and: 'creator is anonymized'
        def courseExecutionDtoResult = courseExecutionFunctionalities.getCourseExecutionByAggregateId(courseExecutionDto.getAggregateId())
        courseExecutionDtoResult.getStudents().find{it.aggregateId == userDto1.aggregateId}.name == ANONYMOUS
        courseExecutionDtoResult.getStudents().find{it.aggregateId == userDto1.aggregateId}.username == ANONYMOUS
        and: 'the tournament is inactive and the creator anonymized'
        def tournamentDtoResult = tournamentFunctionalities.findTournament(tournamentDto.getAggregateId())
        tournamentDtoResult.state == Aggregate.AggregateState.INACTIVE.toString()
        tournamentDtoResult.creator.name == ANONYMOUS
        tournamentDtoResult.creator.username == ANONYMOUS
        and: 'there are no participants'
        tournamentDtoResult.getParticipants().size() == 0
    }

    def 'concurrent delete tournament and add student' () {
        given: 'delete tournament'
        tournamentFunctionalities.removeTournament(tournamentDto.aggregateId)
        and: 'the version number is decreased to simulate concurrency'
        versionService.decrementVersionNumber()

        when: 'a student is concurrently added to the tournament'
        tournamentFunctionalities.addParticipant(tournamentDto.getAggregateId(), userDto2.getAggregateId())

        then: 'fails during merge because the most recent version of the tournament is deleted'
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.AGGREGATE_DELETED
    }

    def 'concurrent change of tournament topics' () {
        given: 'update topics to topic 2'
        def updateTournamentDto = new TournamentDto()
        updateTournamentDto.setAggregateId(tournamentDto.aggregateId)
        updateTournamentDto.setNumberOfQuestions(1)
        def topics =  new HashSet<>(Arrays.asList(topicDto2.aggregateId))
        tournamentFunctionalities.updateTournament(updateTournamentDto, topics)
        and: 'the version number is decreased to simulate concurrency'
        versionService.decrementVersionNumber()

        when: 'update topics to topic 3'
        topics =  new HashSet<>(Arrays.asList(topicDto3.aggregateId));
        tournamentFunctionalities.updateTournament(updateTournamentDto, topics)

        then: 'the quiz is updated'
        def quizDtoResult = quizFunctionalities.findQuiz(tournamentDto.quiz.aggregateId)
        quizDtoResult.questionDtos.size() == 1
        quizDtoResult.questionDtos.get(0).aggregateId == questionDto3.aggregateId
        and: 'the tournament topics are updated and it refers to the new quiz'
        def tournamentDtoResult = tournamentFunctionalities.findTournament(tournamentDto.getAggregateId())
        tournamentDtoResult.topics.size() == 1
        tournamentDtoResult.topics.find{it.aggregateId == topicDto3.aggregateId} != null
        tournamentDtoResult.quiz.aggregateId == tournamentDto.quiz.aggregateId
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}