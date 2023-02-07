package pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.tournament


import org.springframework.boot.test.context.SpringBootTest
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentCourseExecution
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentCreator
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipant
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipantAnswer
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentQuiz
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentTopic
import pt.ulisboa.tecnico.socialsoftware.blcm.SpockTest
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler
import spock.lang.Shared
import spock.lang.Unroll

import java.time.LocalDateTime

@SpringBootTest
class MergeUnitTest extends SpockTest {

    public static final Integer COURSE_EXECUTION_AGGREGATE_ID_1 = 1
    public static final Integer COURSE_EXECUTION_AGGREGATE_ID_2 = 2
    public static final Integer COURSE_EXECUTION_AGGREGATE_ID_3 = 3
    public static final Integer TOPIC_AGGREGATE_ID_1 = 4
    public static final Integer TOPIC_AGGREGATE_ID_2 = 5
    public static final Integer TOPIC_AGGREGATE_ID_3 = 6
    public static final Integer USER_AGGREGATE_ID_1 = 7
    public static final Integer USER_AGGREGATE_ID_2 = 8
    public static final Integer USER_AGGREGATE_ID_3 = 9
    public static final Integer TOURNAMENT_AGGREGATE_ID_1 = 10
    public static final Integer TOURNAMENT_AGGREGATE_ID_2 = 11
    public static final Integer TOURNAMENT_AGGREGATE_ID_3 = 12
    public static final Integer QUIZ_AGGREGATE_ID_1 = 13
    public static final Integer QUIZ_AGGREGATE_ID_2 = 14
    public static final Integer QUIZ_AGGREGATE_ID_3 = 15

    public static final String USER_NAME_1 = "USER_NAME_1"
    public static final String USER_NAME_2 = "USER_NAME_2"
    public static final String USER_NAME_3 = "USER_NAME_3"

    public static final String USER_USERNAME_1 = "USER_USERNAME_1"
    public static final String USER_USERNAME_2 = "USER_USERNAME_2"
    public static final String USER_USERNAME_3 = "USER_USERNAME_3"

    public static final String ACRONYM_1 = "ACRONYM_1"
    public static final String ACRONYM_2 = "ACRONYM_2"
    public static final String ACRONYM_3 = "ACRONYM_3"

    public static final String TOPIC_NAME_1 = "TOPIC_NAME_1"
    public static final String TOPIC_NAME_2 = "TOPIC_NAME_2"
    public static final String TOPIC_NAME_3 = "TOPIC_NAME_3"

    public static final LocalDateTime TIME_1 = DateHandler.now().plusMinutes(5)
    public static final LocalDateTime TIME_2 = DateHandler.now().plusMinutes(25)
    public static final LocalDateTime TIME_3 = DateHandler.now().plusHours(1).plusMinutes(5)
    public static final LocalDateTime TIME_4 = DateHandler.now().plusHours(1).plusMinutes(25)

    public static final String EVENT_1 = "EVENT_1"
    public static final String EVENT_2 = "EVENT_2"
    public static final String EVENT_3 = "EVENT_3"
    public static final String EVENT_4 = "EVENT_4"



    @Shared
    Tournament tournament1
    @Shared
    Tournament tournament2
    @Shared
    Tournament tournament3


    @Shared
    TournamentTopic topic1
    @Shared
    TournamentTopic topic2
    @Shared
    TournamentTopic topic3

    @Shared
    Set<TournamentTopic> topicSetHasTopics1and2 = new HashSet<>()
    @Shared
    Set<TournamentTopic> topicSetHasTopics1 = new HashSet<>()
    @Shared
    Set<TournamentTopic> topicSetHasTopics2 = new HashSet<>()
    @Shared
    Set<TournamentTopic> topicSetHasTopics1and2and3 = new HashSet<>()
    @Shared
    Set<TournamentTopic> topicSetHasTopics1and3 = new HashSet<>()
    @Shared
    Set<TournamentTopic> topicSetHasTopics2and3 = new HashSet<>()
    @Shared
    Set<TournamentTopic> topicSetIsEmpty = new HashSet<>()



    @Shared
    TournamentParticipant participant1
    @Shared
    TournamentParticipant participant2
    @Shared
    TournamentParticipant participant3

    @Shared
    Set<TournamentParticipant> participantSet1 = new HashSet<>()
    @Shared
    Set<TournamentParticipant> participantSet2 = new HashSet<>()
    @Shared
    Set<TournamentParticipant> participantSet3 = new HashSet<>()
    @Shared
    Set<TournamentParticipant> participantSet4 = new HashSet<>()
    @Shared
    Set<TournamentParticipant> participantSet5 = new HashSet<>()
    @Shared
    Set<TournamentParticipant> participantSet6 = new HashSet<>()
    @Shared
    Set<TournamentParticipant> participantSet7 = new HashSet<>()


    def setup() {
        TournamentDto tournamentDto1 = new TournamentDto()
        tournamentDto1.setStartTime(DateHandler.toISOString(TIME_1))
        tournamentDto1.setEndTime(DateHandler.toISOString(TIME_2))
        tournamentDto1.setNumberOfQuestions(5)

        TournamentCreator tournamentCreator1 = new TournamentCreator()
        tournamentCreator1.setAggregateId(USER_AGGREGATE_ID_1)
        tournamentCreator1.setName(USER_NAME_1)
        tournamentCreator1.setUsername(USER_NAME_2)

        TournamentCourseExecution tournamentCourseExecution1 = new TournamentCourseExecution()
        tournamentCourseExecution1.setAggregateId(COURSE_EXECUTION_AGGREGATE_ID_1)
        tournamentCourseExecution1.setVersion(6)
        tournamentCourseExecution1.setAcronym(ACRONYM_1)


        topic1 = new TournamentTopic()
        topic1.setAggregateId(TOPIC_AGGREGATE_ID_1)
        topic1.setName(TOPIC_NAME_1)
        topic1.setVersion(1)

        topic2 = new TournamentTopic()
        topic2.setAggregateId(TOPIC_AGGREGATE_ID_2)
        topic2.setName(TOPIC_NAME_2)
        topic2.setVersion(2)


        topic3 = new TournamentTopic()
        topic3.setAggregateId(TOPIC_AGGREGATE_ID_3)
        topic3.setName(TOPIC_NAME_3)
        topic3.setVersion(3)


        topicSetHasTopics1and2.add(topic1)
        topicSetHasTopics1and2.add(topic2)

        topicSetHasTopics1.add(topic1)

        topicSetHasTopics2.add(topic2)

        topicSetHasTopics1and2and3.add(topic1)
        topicSetHasTopics1and2and3.add(topic2)
        topicSetHasTopics1and2and3.add(topic3)

        topicSetHasTopics1and3.add(topic1)
        topicSetHasTopics1and3.add(topic3)

        topicSetHasTopics2and3.add(topic2)
        topicSetHasTopics2and3.add(topic3)

        topicSetIsEmpty.add(null)


        def ansId1 = 25
        def ansVer1 = 50
        def ansNoAnswered1 = 5
        def ansNoCorrect1 = 3

        def ansId2 = 30
        def ansVer2 = 65
        def ansNoAnswered2 = 4
        def ansNoCorrect2 = 4

        def answer1 = new TournamentParticipantAnswer()
        answer1.setAggregateId(ansId1)
        answer1.setVersion(ansVer1)
        answer1.setNumberOfAnswered(ansNoAnswered1)
        answer1.setNumberOfCorrect(ansNoCorrect1)

        def answer1v2 = new TournamentParticipantAnswer()
        answer1v2.setAggregateId(ansId1)
        answer1v2.setVersion(ansVer2)
        answer1v2.setNumberOfAnswered(ansNoAnswered2)
        answer1v2.setNumberOfCorrect(ansNoCorrect2)



        participant1 = new TournamentParticipant()
        participant1.setAggregateId(USER_AGGREGATE_ID_1)
        participant1.setName(USER_NAME_1)
        participant1.setUsername(USER_USERNAME_1)
        participant1.setVersion(1)
        participant1.setAnswer(answer1)

        participant2 = new TournamentParticipant()
        participant2.setAggregateId(USER_AGGREGATE_ID_2)
        participant2.setName(USER_NAME_2)
        participant2.setUsername(USER_USERNAME_2)
        participant2.setVersion(2)
        participant2.setAnswer(answer1)


        participant3 = new TournamentParticipant()
        participant3.setAggregateId(USER_AGGREGATE_ID_3)
        participant3.setName(USER_NAME_3)
        participant3.setUsername(USER_USERNAME_3)
        participant3.setVersion(3)
        participant3.setAnswer(answer1)


        participantSet1.add(participant1)
        participantSet1.add(participant2)

        participantSet2.add(participant1)

        participantSet3.add(participant2)

        participantSet4.add(participant1)
        participantSet4.add(participant2)
        participantSet4.add(participant3)

        participantSet5.add(participant1)
        participantSet5.add(participant3)

        participantSet6.add(participant2)
        participantSet6.add(participant3)

        participantSet7.add(null)

        TournamentQuiz tournamentQuiz = new TournamentQuiz()
        tournamentQuiz.setAggregateId(QUIZ_AGGREGATE_ID_1)
        tournamentQuiz.setVersion(60)

        tournament1 = new Tournament(TOURNAMENT_AGGREGATE_ID_1, tournamentDto1, tournamentCreator1, tournamentCourseExecution1, topicSetHasTopics1and2, tournamentQuiz)

        tournament2 = new Tournament(tournament1)
        tournament3 = new Tournament(tournament1)
    }

    def cleanup() {
    }

    @Unroll
    def 'intention fail combination' () {
        given:
        def prev = tournament1
        def v1 = tournament2
        def v2 = tournament3

        v1.setStartTime(v1StartTime)
        v2.setStartTime(v2StartTime)

        v1.setEndTime(v1EndTime)
        v2.setEndTime(v2EndTime)

        v1.setNumberOfQuestions(v1NoQuestions)
        v2.setNumberOfQuestions(v2NoQuestions)

        v1.setTopics(v1Topics)
        v2.setTopics(v2Topics)

        when:
        def mergedTournament = (Tournament)v1.merge(v2)

        then:
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.AGGREGATE_MERGE_FAILURE_DUE_TO_INTENSIONS_CONFLICT

        where:
//      Prev values
//      TIME_1      | TIME_1      | TIME_2    | TIME_2    | 5             | 5             | Set.of(topic1, topic2) | Set.of(topic1, topic2)
        v1StartTime | v2StartTime | v1EndTime | v2EndTime | v1NoQuestions | v2NoQuestions | v1Topics               | v2Topics
        TIME_3      | TIME_1      | TIME_2    | TIME_4    | 5             | 5             | topicSetHasTopics1and2 | topicSetHasTopics1and2 // start/end
        TIME_3      | TIME_1      | TIME_2    | TIME_2    | 5             | 4             | topicSetHasTopics1and2 | topicSetHasTopics1and2 // start/numberofquestions
        TIME_1      | TIME_1      | TIME_3    | TIME_2    | 5             | 6             | topicSetHasTopics1and2 | topicSetHasTopics1and2 // end/numberofquestions
        TIME_1      | TIME_1      | TIME_2    | TIME_2    | 1             | 5             | topicSetHasTopics1and2 | topicSetHasTopics2 // numberofquestions/topics
    }


    @Unroll
    def 'intention pass combination' () {
        given:
        def prev = tournament1
        def v1 = tournament2
        def v2 = tournament3

        v1.setStartTime(v1StartTime)
        v2.setStartTime(v2StartTime)

        v1.setEndTime(v1EndTime)
        v2.setEndTime(v2EndTime)

        v1.setNumberOfQuestions(v1NoQuestions)
        v2.setNumberOfQuestions(v2NoQuestions)

        v1.setTopics(v1Topics)
        v2.setTopics(v2Topics)

        when:
        def mergedTournament = (Tournament)v1.merge(v2)

        then:
        mergedTournament != null

        where:
//      Prev values
//      TIME_1      | TIME_1      | TIME_2    | TIME_2    | 5             | 5             | Set.of(topic1, topic2) | Set.of(topic1, topic2)
        v1StartTime | v2StartTime | v1EndTime | v2EndTime | v1NoQuestions | v2NoQuestions | v1Topics               | v2Topics
        TIME_3      | TIME_1      | TIME_4    | TIME_4    | 5             | 5             | topicSetHasTopics1and2 | topicSetHasTopics1and2 // start && end/end
        TIME_3      | TIME_1      | TIME_2    | TIME_2    | 3             | 4             | topicSetHasTopics1and2 | topicSetHasTopics1and2 // start && numberofquestions/numberofquestions
        TIME_1      | TIME_1      | TIME_3    | TIME_2    | 3             | 6             | topicSetHasTopics1and2 | topicSetHasTopics1and2 // end && numberofquestions/numberofquestions
        TIME_1      | TIME_1      | TIME_2    | TIME_2    | 1             | 5             | topicSetHasTopics1     | topicSetHasTopics2 // numberofquestions && topics/topics
        TIME_3      | TIME_1      | TIME_2    | TIME_2    | 5             | 5             | topicSetHasTopics1and2 | topicSetHasTopics1 // start / topics
        TIME_1      | TIME_1      | TIME_3    | TIME_2    | 5             | 5             | topicSetHasTopics1and2 | topicSetHasTopics1 // end / topics
    }

    def 'startTime merge' () {
        given:
        def prev = tournament1
        def v1 = tournament2
        def v2 = tournament3

        v1.setStartTime(TIME_3)
        v2.setStartTime(TIME_4)

        when:
        def mergedTournament = (Tournament)v1.merge(v2)

        then:
        mergedTournament.getStartTime() == TIME_3 // merge is possible so it prevails the most recent version, v1 which isnt committed yet
    }

    def 'endTime merge' () {
        given:
        def prev = tournament1
        def v1 = tournament2
        def v2 = tournament3

        v1.setEndTime(TIME_3)
        v2.setEndTime(TIME_4)

        when:
        def mergedTournament = (Tournament)v1.merge(v2)

        then:
        mergedTournament.getEndTime() == TIME_3 // merge is possible so it prevails the most recent version, v1 which isnt committed yet
    }

    def 'numberOfQuestions merge' () {
        given:
        def prev = tournament1
        def v1 = tournament2
        def v2 = tournament3

        v1.setNumberOfQuestions(3)
        v2.setNumberOfQuestions(4)

        when:
        def mergedTournament = (Tournament)v1.merge(v2)

        then:
        mergedTournament.getNumberOfQuestions() == 3 // merge is possible so it prevails the mos recent version, v1 which isnt committed yet
    }

    @Unroll
    def 'topics merge' () {
        given:
        def prev = tournament1
        def v1 = tournament2
        def v2 = tournament3

        prev.setTopics(topicSetHasTopics1and2)
        v1.setTopics(v1Topics)
        v2.setTopics(v2Topics)

        when:
        def mergedTournament = (Tournament)v1.merge(v2)

        then:

        mergedTournament.getTopics() == mergedTopics

        where:
        v1Topics               | v2Topics                   | mergedTopics
        topicSetHasTopics1and2 | topicSetHasTopics1and2     | topicSetHasTopics1and2
        topicSetHasTopics1     | topicSetHasTopics2         | topicSetHasTopics1
        topicSetHasTopics1and2 | topicSetHasTopics1         | topicSetHasTopics1
        topicSetHasTopics1and2 | topicSetHasTopics2         | topicSetHasTopics2
        topicSetHasTopics1and2 | topicSetHasTopics1and2and3 | topicSetHasTopics1and2and3
        topicSetHasTopics2     | topicSetHasTopics1and2and3 | topicSetHasTopics2
        topicSetHasTopics1     | topicSetHasTopics1and2and3 | topicSetHasTopics1
    }

    @Unroll
    def 'participants merge' () {
        given:
        def prev = tournament1
        def v1 = tournament2
        def v2 = tournament3

        prev.setParticipants(participantSet1)
        v1.setParticipants(v1Participants)
        v2.setParticipants(v2Participants)

        when:
        def mergedTournament = (Tournament)v1.merge(v2)

        then:
        mergedTournament.getParticipants() == mergedParticipants

        where:
        v1Participants  | v2Participants    | mergedParticipants
        participantSet1 | participantSet1   | participantSet1
        participantSet2 | participantSet3   | new HashSet<TournamentParticipant>()
        participantSet1 | participantSet2   | participantSet2
        participantSet1 | participantSet3   | participantSet3
        participantSet1 | participantSet4   | participantSet4
        participantSet3 | participantSet4   | participantSet6
        participantSet2 | participantSet4   | participantSet5
    }

    def 'participants merge with different versions of the same participant _one is anonymous_' () {
        given:
        def prev = tournament1
        def v1 = tournament2
        def v2 = tournament3

        def ansId1 = 25
        def ansVer1 = 50
        def ansNoAnswered1 = 5
        def ansNoCorrect1 = 3

        def ansId2 = 30
        def ansVer2 = 65
        def ansNoAnswered2 = 4
        def ansNoCorrect2 = 4

        def answer1 = new TournamentParticipantAnswer()
        answer1.setAggregateId(ansId1)
        answer1.setVersion(ansVer1)
        answer1.setNumberOfAnswered(ansNoAnswered1)
        answer1.setNumberOfCorrect(ansNoCorrect1)

        def answer1v2 = new TournamentParticipantAnswer()
        answer1v2.setAggregateId(ansId1)
        answer1v2.setVersion(ansVer2)
        answer1v2.setNumberOfAnswered(ansNoAnswered2)
        answer1v2.setNumberOfCorrect(ansNoCorrect2)

        def participant1v2 = new TournamentParticipant()
        participant1v2.setAggregateId(participant1.getAggregateId())
        participant1v2.setName("ANONYMOUS")
        participant1v2.setUsername("ANONYMOUS")
        participant1v2.setVersion(participant1.getVersion() + 1)

        HashSet<TournamentParticipant> prevParticipants = new HashSet<>()
        prevParticipants.add(participant1)

        HashSet<TournamentParticipant> v1Participants = new HashSet<>()
        v1Participants.add(participant1)

        HashSet<TournamentParticipant> v2Participants = new HashSet<>()
        v2Participants.add(participant1v2)


        prev.setParticipants(prevParticipants)
        v1.setParticipants(v1Participants)
        v2.setParticipants(v2Participants)

        when:
        def mergedTournament = (Tournament)(v1.merge(v2))

        then:
        mergedTournament.getParticipants() == new HashSet(v2Participants)

    }

    def 'participants merge with different versions of the same participant _one has answers_' () {
        given:
        def prev = tournament1
        def v1 = tournament2
        def v2 = tournament3

        def ansId1 = 25
        def ansVer1 = 50
        def ansNoAnswered1 = 5
        def ansNoCorrect1 = 3

        def ansId2 = 30
        def ansVer2 = 65
        def ansNoAnswered2 = 4
        def ansNoCorrect2 = 4

        def answer1 = new TournamentParticipantAnswer()
        answer1.setAggregateId(ansId1)
        answer1.setVersion(ansVer1)
        answer1.setNumberOfAnswered(ansNoAnswered1)
        answer1.setNumberOfCorrect(ansNoCorrect1)

        def answer1v2 = new TournamentParticipantAnswer()
        answer1v2.setAggregateId(ansId1)
        answer1v2.setVersion(ansVer2)
        answer1v2.setNumberOfAnswered(ansNoAnswered2)
        answer1v2.setNumberOfCorrect(ansNoCorrect2)


        def participant1v2 = new TournamentParticipant()
        participant1v2.setAggregateId(participant1.getAggregateId())
        participant1v2.setName(participant1.getName())
        participant1v2.setUsername(participant1.getUsername())
        participant1v2.setVersion(participant1.getVersion() + 1)

        participant1.setAnswer(answer1)
        participant1v2.setAnswer(answer1v2)


        HashSet<TournamentParticipant> prevParticipants = new HashSet<TournamentParticipant>()
        prevParticipants.add(participant1)

        HashSet<TournamentParticipant> v1Participants = new HashSet<TournamentParticipant>()
        v1Participants.add(participant1)


        HashSet<TournamentParticipant> v2Participants = new HashSet<>()
        v2Participants.add(participant1v2)


        prev.setParticipants(prevParticipants)
        v1.setParticipants(v1Participants)
        v2.setParticipants(v2Participants)

        when:
        def mergedTournament = (Tournament)(v1.merge(v2))

        then:
        mergedTournament.getParticipants() == new HashSet(v2Participants)
    }

}