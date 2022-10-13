package pt.ulisboa.tecnico.socialsoftware.blcm.tournament


import org.springframework.boot.test.context.SpringBootTest
import pt.ulisboa.tecnico.socialsoftware.blcm.SpockTest
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentCourseExecution
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentCreator
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipant
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipantAnswer
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentQuiz
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentTopic
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto
import spock.lang.Shared
import spock.lang.Unroll

import java.time.LocalDateTime
import java.util.stream.Collectors

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

    public static final LocalDateTime TIME_1 = LocalDateTime.of(2023, 8, 15, 22, 0)
    public static final LocalDateTime TIME_2 = LocalDateTime.of(2023, 8, 15, 22, 20)
    public static final LocalDateTime TIME_3 = LocalDateTime.of(2023, 8, 15, 23, 00)
    public static final LocalDateTime TIME_4 = LocalDateTime.of(2023, 8, 15, 23, 20)


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
    Set<TournamentTopic> topicSet1 = new HashSet<>()
    @Shared
    Set<TournamentTopic> topicSet2 = new HashSet<>()
    @Shared
    Set<TournamentTopic> topicSet3 = new HashSet<>()
    @Shared
    Set<TournamentTopic> topicSet4 = new HashSet<>()
    @Shared
    Set<TournamentTopic> topicSet5 = new HashSet<>()
    @Shared
    Set<TournamentTopic> topicSet6 = new HashSet<>()
    @Shared
    Set<TournamentTopic> topicSet7 = new HashSet<>()



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
        tournamentDto1.setStartTime(TIME_1.toString())
        tournamentDto1.setEndTime(TIME_2.toString())
        tournamentDto1.setNumberOfQuestions(5)

        TournamentCreator tournamentCreator1 = new TournamentCreator()
        tournamentCreator1.setAggregateId(USER_AGGREGATE_ID_1)
        tournamentCreator1.setName(USER_NAME_1)
        tournamentCreator1.setUsername(USER_NAME_2)

        TournamentCourseExecution tournamentCourseExecution1 = new TournamentCourseExecution()
        tournamentCourseExecution1.setAggregateId(COURSE_EXECUTION_AGGREGATE_ID_1)
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


        topicSet1.add(topic1)
        topicSet1.add(topic2)

        topicSet2.add(topic1)

        topicSet3.add(topic2)

        topicSet4.add(topic1)
        topicSet4.add(topic2)
        topicSet4.add(topic3)

        topicSet5.add(topic1)
        topicSet5.add(topic3)

        topicSet6.add(topic2)
        topicSet6.add(topic3)

        topicSet7.add(null)



        participant1 = new TournamentParticipant()
        participant1.setAggregateId(USER_AGGREGATE_ID_1)
        participant1.setName(USER_NAME_1)
        participant1.setUsername(USER_USERNAME_1)
        participant1.setVersion(1)

        participant2 = new TournamentParticipant()
        participant2.setAggregateId(USER_AGGREGATE_ID_2)
        participant2.setName(USER_NAME_2)
        participant1.setUsername(USER_USERNAME_2)
        participant2.setVersion(2)


        participant3 = new TournamentParticipant()
        participant3.setAggregateId(USER_AGGREGATE_ID_3)
        participant3.setName(USER_NAME_3)
        participant1.setUsername(USER_USERNAME_3)
        participant3.setVersion(3)


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

        tournament1 = new Tournament(TOURNAMENT_AGGREGATE_ID_1, tournamentDto1, tournamentCreator1, tournamentCourseExecution1, topicSet1, tournamentQuiz)

        tournament2 = new Tournament(tournament1)
        tournament3 = new Tournament(tournament1)
    }

    def cleanup() {

    }

    def 'startTime intention' () {
        given:
        def prev = tournament1
        def v1 = tournament2
        def v2 = tournament3

        v1.setStartTime(TIME_3)
        v2.setStartTime(TIME_4)

        when:
        def mergedTournament = (Tournament)v1.merge(v2)

        then:
        mergedTournament.getStartTime() == TIME_3 // merge is possible so it prevails the mos recent version, v1 which isnt committed yet
    }

    def 'endTime intention' () {
        given:
        def prev = tournament1
        def v1 = tournament2
        def v2 = tournament3

        v1.setEndTime(TIME_3)
        v2.setEndTime(TIME_4)

        when:
        def mergedTournament = (Tournament)v1.merge(v2)

        then:
        mergedTournament.getEndTime() == TIME_3 // merge is possible so it prevails the mos recent version, v1 which isnt committed yet
    }

    def 'number of questions intention' () {
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
    def 'topics intention and merge' () {
        given:
        def prev = tournament1
        def v1 = tournament2
        def v2 = tournament3

        prev.setTopics(topicSet1)
        v1.setTopics(v1Topics)
        v2.setTopics(v2Topics)

        when:
        def mergedTournament = (Tournament)v1.merge(v2)

        then:

        mergedTournament.getTopics() == mergedTopics

        where:
        v1Topics    | v2Topics  | mergedTopics
        topicSet1   | topicSet1 | topicSet1
        topicSet2   | topicSet3 | new HashSet<TournamentTopic>()
        topicSet1   | topicSet2 | topicSet2
        topicSet1   | topicSet3 | topicSet3
        topicSet1   | topicSet4 | topicSet4
        topicSet3   | topicSet4 | topicSet6
        topicSet2   | topicSet4 | topicSet5
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
        error.errorMessage == ErrorMessage.TOURNAMENT_MERGE_FAILURE

        where:
        /** Prev values
        TIME_1          | TIME_1            | TIME_2        | TIME_2        | 5                 | 5                 | Set.of(topic1, topic2)    | Set.of(topic1, topic2)
         **/
        v1StartTime | v2StartTime   | v1EndTime | v2EndTime | v1NoQuestions | v2NoQuestions | v1Topics  | v2Topics
        TIME_3      | TIME_1        | TIME_2    | TIME_4    | 5             | 5             | topicSet1 | topicSet1 // start/end
        TIME_3      | TIME_1        | TIME_2    | TIME_2    | 3             | 4             | topicSet1 | topicSet1 // start/no
        TIME_3      | TIME_1        | TIME_2    | TIME_2    | 5             | 5             | topicSet2 | topicSet3 // start/topics
        TIME_1      | TIME_1        | TIME_3    | TIME_4    | 2             | 6             | topicSet1 | topicSet1 // end/no
        TIME_1      | TIME_1        | TIME_3    | TIME_4    | 5             | 5             | topicSet2 | topicSet3 // end/topics
        TIME_1      | TIME_1        | TIME_2    | TIME_2    | 1             | 4             | topicSet2 | topicSet3 // no/topics
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
        println("prev:" + prev.getParticipants().stream().map(t -> t.getName()).collect(Collectors.toSet()))
        println("v1:" + v1Participants.stream().map(t -> t.getName()).collect(Collectors.toSet()))
        println("v2:" + v2Participants.stream().map(t -> t.getName()).collect(Collectors.toSet()))
        println("tournament:" + mergedTournament.getParticipants().stream().map(t -> t.getName()).collect(Collectors.toSet()))
        println("calculated:" + mergedParticipants.stream().map(t -> t.getName()).collect(Collectors.toSet()))

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

    @Unroll
    def 'participants merge with answers' () {
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

        //participant1.setAnswer(answer1)

        participant2.setAggregateId(participant1.getAggregateId())
        //participant2.setAnswer(answer1v2)
        participant2.setName(participant1.getName())
        participant1.setUsername(participant1.getUsername())



        HashSet<TournamentParticipant> prevParticipants = new HashSet<>()
        prevParticipants.add(participant1)

        HashSet<TournamentParticipant> v1Participants = new HashSet<>()
        v1Participants.add(participant1)

        HashSet<TournamentParticipant> v2Participants = new HashSet<>()
        v2Participants.add(participant2)


        prev.setParticipants(prevParticipants)
        v1.setParticipants(v1Participants)
        v2.setParticipants(v2Participants)

        when:
        def mergedTournament = (Tournament)(v1.merge(v2))

        then:
        prev.getParticipants() == v1.getParticipants()
        /*println("prev:" + prev.getParticipants().stream().map(p -> p.getAnswer().getVersion()).collect(Collectors.toSet()))
        println("v1:" + v1.getParticipants().stream().map(p -> p.getAnswer().getVersion()).collect(Collectors.toSet()))
        println("v2:" + v2.getParticipants().stream().map(p -> p.getAnswer().getVersion()).collect(Collectors.toSet()))
        println("tournament:" + mergedTournament.getParticipants().stream().map(p -> p.getAnswer().getVersion()).collect(Collectors.toSet()))
        println("calculated:" + Set<TournamentParticipant>.of(participant2).stream().map(p -> p.getAnswer().getVersion()).collect(Collectors.toSet()))
        mergedTournament.getParticipants() == new HashSet(v2Participants)*/

    }
}