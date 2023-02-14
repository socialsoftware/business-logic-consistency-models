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

@SpringBootTest
class MergeUnitTest extends SpockTest {
    public static final Integer COURSE_EXECUTION_AGGREGATE_ID_1 = 1
    public static final Integer TOPIC_AGGREGATE_ID_1 = 4
    public static final Integer TOPIC_AGGREGATE_ID_2 = 5
    public static final Integer TOPIC_AGGREGATE_ID_3 = 6
    public static final Integer USER_AGGREGATE_ID_1 = 7
    public static final Integer USER_AGGREGATE_ID_2 = 8
    public static final Integer USER_AGGREGATE_ID_3 = 9
    public static final Integer TOURNAMENT_AGGREGATE_ID_1 = 10
    public static final Integer QUIZ_AGGREGATE_ID_1 = 13

    public static final String USER_NAME_1 = "USER_NAME_1"
    public static final String USER_NAME_2 = "USER_NAME_2"
    public static final String USER_NAME_3 = "USER_NAME_3"

    public static final String USER_USERNAME_1 = "USER_USERNAME_1"
    public static final String USER_USERNAME_2 = "USER_USERNAME_2"
    public static final String USER_USERNAME_3 = "USER_USERNAME_3"

    public static final String ACRONYM_1 = "ACRONYM_1"

    public static final String TOPIC_NAME_1 = "TOPIC_NAME_1"
    public static final String TOPIC_NAME_2 = "TOPIC_NAME_2"
    public static final String TOPIC_NAME_3 = "TOPIC_NAME_3"

    Tournament tournament1, tournament2, tournament3
    TournamentTopic topic1, topic2, topic3
    TournamentParticipant participant1, participant2, participant3

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
    Set<TournamentParticipant> participantSetHasParticipants1and2 = new HashSet<>()
    @Shared
    Set<TournamentParticipant> participantSetHasParticipants1 = new HashSet<>()
    @Shared
    Set<TournamentParticipant> participantSetHasParticipants2 = new HashSet<>()
    @Shared
    Set<TournamentParticipant> participantSetHasParticipants1and2and3 = new HashSet<>()
    @Shared
    Set<TournamentParticipant> participantSetHasParticipants1and3 = new HashSet<>()
    @Shared
    Set<TournamentParticipant> participantSetHasParticipants2and3 = new HashSet<>()
    @Shared
    Set<TournamentParticipant> participantSetIsEmpty = new HashSet<>()

    def setup() {
        TournamentDto tournamentDto1 = new TournamentDto()
        tournamentDto1.setStartTime(DateHandler.toISOString(TIME_1))
        tournamentDto1.setEndTime(DateHandler.toISOString(TIME_2))
        tournamentDto1.setNumberOfQuestions(5)

        TournamentCreator tournamentCreator1 = new TournamentCreator()
        tournamentCreator1.setAggregateId(USER_AGGREGATE_ID_1)
        tournamentCreator1.setName(USER_NAME_1)
        tournamentCreator1.setUsername(USER_USERNAME_1)

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

        def answer1Committed = new TournamentParticipantAnswer()
        answer1Committed.setAggregateId(ansId1)
        answer1Committed.setVersion(ansVer2)
        answer1Committed.setNumberOfAnswered(ansNoAnswered2)
        answer1Committed.setNumberOfCorrect(ansNoCorrect2)

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

        participantSetIsEmpty.add(null)

        participantSetHasParticipants1.add(participant1)

        participantSetHasParticipants2.add(participant2)

        participantSetHasParticipants1and2.add(participant1)
        participantSetHasParticipants1and2.add(participant2)

        participantSetHasParticipants1and3.add(participant1)
        participantSetHasParticipants1and3.add(participant3)

        participantSetHasParticipants2and3.add(participant2)
        participantSetHasParticipants2and3.add(participant3)

        participantSetHasParticipants1and2and3.add(participant1)
        participantSetHasParticipants1and2and3.add(participant2)
        participantSetHasParticipants1and2and3.add(participant3)

        TournamentQuiz tournamentQuiz = new TournamentQuiz()
        tournamentQuiz.setAggregateId(QUIZ_AGGREGATE_ID_1)
        tournamentQuiz.setVersion(60)

        tournament1 = new Tournament(TOURNAMENT_AGGREGATE_ID_1, tournamentDto1, tournamentCreator1, tournamentCourseExecution1, topicSetHasTopics1and2, tournamentQuiz)

        tournament2 = new Tournament(tournament1)
        tournament3 = new Tournament(tournament1)
    }

    def cleanup() {
    }

    // merge intentions

    @Unroll
    def 'intention fail combination' () {
        given:
        def prev = tournament1
        def toCommit = tournament2
        def committed = tournament3

        toCommit.setStartTime(toCommitStartTime)
        committed.setStartTime(committedStartTime)

        toCommit.setEndTime(toCommitEndTime)
        committed.setEndTime(committedEndTime)

        toCommit.setNumberOfQuestions(toCommitNoQuestions)
        committed.setNumberOfQuestions(committedNoQuestions)

        toCommit.setTopics(toCommitTopics)
        committed.setTopics(committedTopics)

        when:
        toCommit.merge(committed)

        then:
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.AGGREGATE_MERGE_FAILURE_DUE_TO_INTENSIONS_CONFLICT

        where:
//      Prev values
//      TIME_1            | TIME_1             | TIME_2          | TIME_2           | 5                   | 5                    | Set.of(topic1, topic2) | Set.of(topic1, topic2)
        toCommitStartTime | committedStartTime | toCommitEndTime | committedEndTime | toCommitNoQuestions | committedNoQuestions | toCommitTopics         | committedTopics
        TIME_3            | TIME_1             | TIME_2          | TIME_4           | 5                   | 5                    | topicSetHasTopics1and2 | topicSetHasTopics1and2 // start/end
        TIME_3            | TIME_1             | TIME_2          | TIME_2           | 5                   | 4                    | topicSetHasTopics1and2 | topicSetHasTopics1and2 // start/numberofquestions
        TIME_1            | TIME_1             | TIME_3          | TIME_2           | 5                   | 6                    | topicSetHasTopics1and2 | topicSetHasTopics1and2 // end/numberofquestions
        TIME_1            | TIME_1             | TIME_2          | TIME_2           | 1                   | 5                    | topicSetHasTopics1and2 | topicSetHasTopics2 // numberofquestions/topics
    }


    @Unroll
    def 'intention pass combination' () {
        given:
        def prev = tournament1
        def toCommit = tournament2
        def committed = tournament3

        toCommit.setStartTime(toCommitStartTime)
        committed.setStartTime(committedStartTime)

        toCommit.setEndTime(toCommitEndTime)
        committed.setEndTime(committedEndTime)

        toCommit.setNumberOfQuestions(toCommitNoQuestions)
        committed.setNumberOfQuestions(committedNoQuestions)

        toCommit.setTopics(toCommitTopics)
        committed.setTopics(committedTopics)

        when:
        def mergedTournament = (Tournament)toCommit.merge(committed)

        then:
        mergedTournament != null

        where:
//      Prev values
//      TIME_1            | TIME_1             | TIME_2          | TIME_2           | 5                   | 5                    | Set.of(topic1, topic2) | Set.of(topic1, topic2)
        toCommitStartTime | committedStartTime | toCommitEndTime | committedEndTime | toCommitNoQuestions | committedNoQuestions | toCommitTopics         | committedTopics
        TIME_3            | TIME_1             | TIME_4          | TIME_4           | 5                   | 5                    | topicSetHasTopics1and2 | topicSetHasTopics1and2 // start && end/end
        TIME_3            | TIME_1             | TIME_2          | TIME_2           | 3                   | 4                    | topicSetHasTopics1and2 | topicSetHasTopics1and2 // start && numberofquestions/numberofquestions
        TIME_1            | TIME_1             | TIME_3          | TIME_2           | 3                   | 6                    | topicSetHasTopics1and2 | topicSetHasTopics1and2 // end && numberofquestions/numberofquestions
        TIME_1            | TIME_1             | TIME_2          | TIME_2           | 1                   | 5                    | topicSetHasTopics1     | topicSetHasTopics2 // numberofquestions && topics/topics
        TIME_3            | TIME_1             | TIME_2          | TIME_2           | 5                   | 5                    | topicSetHasTopics1and2 | topicSetHasTopics1 // start / topics
        TIME_1            | TIME_1             | TIME_3          | TIME_2           | 5                   | 5                    | topicSetHasTopics1and2 | topicSetHasTopics1 // end / topics
    }

    // merge operations

    def 'startTime merge' () {
        given:
        def prev = tournament1
        def toCommit = tournament2
        def committed = tournament3

        prev.setStartTime(TIME_1)
        committed.setStartTime(committedTime)
        toCommit.setStartTime(toCommitTime)

        when:
        def mergedTournament = (Tournament)toCommit.merge(committed)

        then:
        mergedTournament.getStartTime() == result

        where:
        committedTime | toCommitTime || result
        TIME_4        | TIME_3       || TIME_3
        TIME_1        | TIME_3       || TIME_3
        TIME_4        | TIME_1       || TIME_4
    }

    def 'endTime merge' () {
        given:
        def prev = tournament1
        def toCommit = tournament2
        def committed = tournament3

        prev.setEndTime(TIME_2)
        committed.setEndTime(committedTime)
        toCommit.setEndTime(toCommitTime)

        when:
        def mergedTournament = (Tournament)toCommit.merge(committed)

        then:
        mergedTournament.getEndTime() == result

        where:
        committedTime | toCommitTime || result
        TIME_4        | TIME_3       || TIME_3
        TIME_2        | TIME_3       || TIME_3
        TIME_4        | TIME_2       || TIME_4
    }

    def 'numberOfQuestions merge' () {
        given:
        def prev = tournament1
        def committed = tournament3
        def toCommit = tournament2

        prev.setNumberOfQuestions(5)
        committed.setNumberOfQuestions(committedValue)
        toCommit.setNumberOfQuestions(toCommitValue)

        when:
        def mergedTournament = (Tournament)toCommit.merge(committed)

        then:
        mergedTournament.getNumberOfQuestions() == result

        where:
        committedValue | toCommitValue || result
        4              | 3             || 3
        5              | 3             || 3
        4              | 5             || 4
    }

    @Unroll
    def 'topics merge' () {
        given:
        def prev = tournament1
        def committed = tournament3
        def toCommit = tournament2

        prev.setTopics(topicSetHasTopics1and2)
        committed.setTopics(committedTopics)
        toCommit.setTopics(toCommitTopics)

        when:
        def mergedTournament = (Tournament)toCommit.merge(committed)

        then:
        mergedTournament.getTopics() == result

        where:
        committedTopics        | toCommitTopics             || result
        topicSetHasTopics1and2 | topicSetHasTopics1and2     || topicSetHasTopics1and2
        topicSetHasTopics2     | topicSetHasTopics1         || topicSetHasTopics1
        topicSetHasTopics1     | topicSetHasTopics1and2     || topicSetHasTopics1
        topicSetHasTopics1and2 | topicSetHasTopics1and2and3 || topicSetHasTopics1and2and3
    }

    @Unroll
    def 'participants merge' () {
        given:
        def prev = tournament1
        def committed = tournament3
        def toCommit = tournament2

        prev.setParticipants(prevPartipants)
        committed.setParticipants(committedParticipants)
        toCommit.setParticipants(toCommitParticipants)

        when:
        def mergedTournament = (Tournament)toCommit.merge(committed)

        then:
        mergedTournament.getParticipants() == result

        where:
        prevPartipants                     | committedParticipants                  | toCommitParticipants                   || result
        participantSetHasParticipants1and2 | participantSetHasParticipants1and2     | participantSetHasParticipants1and2     || participantSetHasParticipants1and2 // no changes
        participantSetHasParticipants1and2 | participantSetHasParticipants1         | participantSetHasParticipants1and2     || participantSetHasParticipants1 // only committed deletes
        participantSetHasParticipants1and2 | participantSetHasParticipants1and2     | participantSetHasParticipants1         || participantSetHasParticipants1 // only to commit deletes
        participantSetHasParticipants1and2 | participantSetHasParticipants2         | participantSetHasParticipants1         || new HashSet<TournamentParticipant>() // two different deletes
        participantSetHasParticipants1and2 | participantSetHasParticipants1         | participantSetHasParticipants1         || participantSetHasParticipants1 // same delete
        participantSetHasParticipants1and2 | participantSetHasParticipants1and2and3 | participantSetHasParticipants1and2     || participantSetHasParticipants1and2and3 // only committed inserts
        participantSetHasParticipants1and2 | participantSetHasParticipants1and2     | participantSetHasParticipants1and2and3 || participantSetHasParticipants1and2and3 // only to commit inserts
        participantSetHasParticipants1     | participantSetHasParticipants1and2     | participantSetHasParticipants1and3     || participantSetHasParticipants1and2and3 // two different inserts
        participantSetHasParticipants1     | participantSetHasParticipants1and2     | participantSetHasParticipants1and2     || participantSetHasParticipants1and2 // same insert
        participantSetHasParticipants1and2 | participantSetHasParticipants1         | participantSetHasParticipants1and2and3 || participantSetHasParticipants1and3 // different committed delete and to commit insert
        participantSetHasParticipants1and2 | participantSetHasParticipants1and2and3 | participantSetHasParticipants1         || participantSetHasParticipants1and3 // different committed insert and to commit delete
    }

    def 'participants merge with different versions of the same participant _one is anonymous_' () {
        given:
        def prev = tournament1
        def committed = tournament3
        def toCommit = tournament2

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

        def answer1Committed = new TournamentParticipantAnswer()
        answer1Committed.setAggregateId(ansId1)
        answer1Committed.setVersion(ansVer2)
        answer1Committed.setNumberOfAnswered(ansNoAnswered2)
        answer1Committed.setNumberOfCorrect(ansNoCorrect2)

        def participant1Committed = new TournamentParticipant()
        participant1Committed.setAggregateId(participant1.getAggregateId())
        participant1Committed.setName(ANONYMOUS)
        participant1Committed.setUsername(ANONYMOUS)
        participant1Committed.setVersion(participant1.getVersion() + 1)

        HashSet<TournamentParticipant> prevParticipants = new HashSet<>()
        prevParticipants.add(participant1)

        HashSet<TournamentParticipant> toCommitParticipants = new HashSet<>()
        toCommitParticipants.add(participant1)

        HashSet<TournamentParticipant> committedParticipants = new HashSet<>()
        committedParticipants.add(participant1Committed)


        prev.setParticipants(prevParticipants)
        toCommit.setParticipants(toCommitParticipants)
        committed.setParticipants(committedParticipants)

        when:
        def mergedTournament = (Tournament)(toCommit.merge(committed))

        then:
        mergedTournament.getParticipants() == new HashSet(committedParticipants)
    }

    def 'participants merge with different versions of the same participant _one has answers_' () {
        given:
        def prev = tournament1
        def toCommit = tournament2
        def committed = tournament3

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

        def answer1Committed = new TournamentParticipantAnswer()
        answer1Committed.setAggregateId(ansId1)
        answer1Committed.setVersion(ansVer2)
        answer1Committed.setNumberOfAnswered(ansNoAnswered2)
        answer1Committed.setNumberOfCorrect(ansNoCorrect2)

        def participant1Committed = new TournamentParticipant()
        participant1Committed.setAggregateId(participant1.getAggregateId())
        participant1Committed.setName(participant1.getName())
        participant1Committed.setUsername(participant1.getUsername())
        participant1Committed.setVersion(participant1.getVersion() + 1)

        participant1.setAnswer(answer1)
        participant1Committed.setAnswer(answer1Committed)

        HashSet<TournamentParticipant> prevParticipants = new HashSet<TournamentParticipant>()
        prevParticipants.add(participant1)

        HashSet<TournamentParticipant> toCommitParticipants = new HashSet<TournamentParticipant>()
        toCommitParticipants.add(participant1)

        HashSet<TournamentParticipant> committedParticipants = new HashSet<>()
        committedParticipants.add(participant1Committed)

        prev.setParticipants(prevParticipants)
        toCommit.setParticipants(toCommitParticipants)
        committed.setParticipants(committedParticipants)

        when:
        def mergedTournament = (Tournament)(toCommit.merge(committed))

        then:
        mergedTournament.getParticipants() == new HashSet(committedParticipants)
    }

}