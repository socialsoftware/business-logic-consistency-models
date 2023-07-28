package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.tcc;

import jakarta.persistence.Entity;
import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.CausalConsistency;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.domain.TournamentParticipant;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.domain.TournamentTopic;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.dto.UserDto;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.ErrorMessage.AGGREGATE_MERGE_FAILURE;

@Entity
public class TournamentTCC extends Tournament implements CausalConsistency {
    public TournamentTCC() {
        super();
    }

    public TournamentTCC(Integer aggregateId, TournamentDto tournamentDto, UserDto creatorDto,
                         CourseExecutionDto courseExecutionDto, Set<TopicDto> topicDtos, QuizDto quizDto) {
        super(aggregateId, tournamentDto, creatorDto, courseExecutionDto, topicDtos, quizDto);
    }

    /* used to update the tournament by creating new versions */
    public TournamentTCC(TournamentTCC other) {
        super(other);
    }

    @Override
    public Set<String> getFieldsChangedByFunctionalities()  {
        return Set.of("startTime", "endTime", "numberOfQuestions", "tournamentTopics", "tournamentParticipants", "cancelled", "tournamentCourseExecution", "tournamentCreator");
    }

    @Override
    public Set<String[]> getIntentions() {
        return Set.of(
                new String[]{"startTime", "endTime"},
                new String[]{"startTime", "numberOfQuestions"},
                new String[]{"endTime", "numberOfQuestions"},
                new String[]{"numberOfQuestions", "tournamentTopics"});
    }

    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields){
        if (!(committedVersion instanceof TournamentTCC)) {
            throw new TutorException(AGGREGATE_MERGE_FAILURE, getAggregateId());
        }

        TournamentTCC committedTournament = (TournamentTCC) committedVersion;
        TournamentTCC mergedTournament = new TournamentTCC(this);

        // merge of creator is built in the participants dont know yet
        mergeCreator(committedTournament, mergedTournament);
        //mergeCourseExecution(committedTournament, mergedTournament);
        mergeCourseExecution(committedTournament, mergedTournament);
        mergeQuiz(committedTournament, mergedTournament);
        mergeCancelled(toCommitVersionChangedFields, committedTournament, mergedTournament);
        mergeStartTime(toCommitVersionChangedFields, committedTournament, mergedTournament);
        mergeEndTime(toCommitVersionChangedFields, committedTournament, mergedTournament);
        mergeNumberOfQuestions(toCommitVersionChangedFields, committedTournament, mergedTournament);
        mergeParticipants((Tournament) getPrev(), this, committedTournament, mergedTournament);
        //mergeTopics((Tournament) getPrev(), this, committedTournament, mergedTournament);
        mergeTopics(toCommitVersionChangedFields, committedTournament, mergedTournament);

        return mergedTournament;
    }

    private void mergeCreator(Tournament committedTournament, Tournament mergedTournament) {
        if (getTournamentCourseExecution().getCourseExecutionVersion() >= committedTournament.getTournamentCourseExecution().getCourseExecutionVersion()) {
            mergedTournament.getTournamentCreator().setCreatorName(getTournamentCreator().getCreatorName());
            mergedTournament.getTournamentCreator().setCreatorUsername(getTournamentCreator().getCreatorUsername());
            mergedTournament.getTournamentCreator().setCreatorVersion(getTournamentCreator().getCreatorVersion());
        } else {
            mergedTournament.getTournamentCreator().setCreatorName(committedTournament.getTournamentCreator().getCreatorName());
            mergedTournament.getTournamentCreator().setCreatorUsername(committedTournament.getTournamentCreator().getCreatorUsername());
            mergedTournament.getTournamentCreator().setCreatorVersion(committedTournament.getTournamentCreator().getCreatorVersion());
        }
    }

    /*private void mergeCourseExecution(Tournament committedTournament, Tournament mergedTournament) {
        if(getCourseExecution().getVersion() >= committedTournament.getCourseExecution().getVersion()) {
            mergedTournament.getCourseExecution().setVersion(getCourseExecution().getVersion());
        } else {
            mergedTournament.getCourseExecution().setVersion(committedTournament.getCourseExecution().getVersion());
        }
    }*/

    private void mergeCourseExecution(Tournament committedTournament, Tournament mergedTournament) {
        if (getTournamentCourseExecution().getCourseExecutionVersion() >= committedTournament.getTournamentCourseExecution().getCourseExecutionVersion()) {
            mergedTournament.getTournamentCourseExecution().setCourseExecutionVersion(getTournamentCourseExecution().getCourseExecutionVersion());
        } else {
            mergedTournament.getTournamentCourseExecution().setCourseExecutionVersion(committedTournament.getTournamentCourseExecution().getCourseExecutionVersion());
        }
    }

    private void mergeQuiz(Tournament committedTournament, Tournament mergedTournament) {
        // The quiz aggregate id must be set in case the quiz has been regenerated due to the previous having been invalidated
        if (getTournamentQuiz().getQuizVersion() >= committedTournament.getTournamentQuiz().getQuizVersion()) {
            mergedTournament.getTournamentQuiz().setQuizAggregateId(getTournamentQuiz().getQuizAggregateId());
            mergedTournament.getTournamentQuiz().setQuizVersion(getTournamentQuiz().getQuizVersion());
        } else {
            mergedTournament.getTournamentQuiz().setQuizVersion(committedTournament.getTournamentQuiz().getQuizAggregateId());
            mergedTournament.getTournamentQuiz().setQuizVersion(committedTournament.getTournamentQuiz().getQuizVersion());
        }

    }

    private void mergeCancelled(Set<String> toCommitVersionChangedFields, Tournament committedTournament, Tournament mergedTournament) {
        if (toCommitVersionChangedFields.contains("cancelled")) {
            mergedTournament.setCancelled(isCancelled());
        } else {
            mergedTournament.setCancelled(committedTournament.isCancelled());
        }
    }

    private void mergeNumberOfQuestions(Set<String> toCommitVersionChangedFields, Tournament committedTournament, Tournament mergedTournament) {
        if (toCommitVersionChangedFields.contains("numberOfQuestions")) {
            mergedTournament.setNumberOfQuestions(getNumberOfQuestions());
        } else {
            mergedTournament.setNumberOfQuestions(committedTournament.getNumberOfQuestions());
        }
    }

    private void mergeEndTime(Set<String> toCommitVersionChangedFields, Tournament committedTournament, Tournament mergedTournament) {
        if (toCommitVersionChangedFields.contains("endTime")) {
            mergedTournament.setEndTime(getEndTime());
        } else {
            mergedTournament.setEndTime(committedTournament.getEndTime());
        }
    }

    private void mergeStartTime(Set<String> toCommitVersionChangedFields, Tournament committedTournament, Tournament mergedTournament) {
        if (toCommitVersionChangedFields.contains("startTime")) {
            mergedTournament.setStartTime(getStartTime());
        } else {
            mergedTournament.setStartTime(committedTournament.getStartTime());
        }
    }

    private void mergeParticipants(Tournament prev, Tournament v1, Tournament v2, Tournament mergedTournament) {
        // Here we "calculate" the result of the incremental fields. This fields will always be the same regardless
        // of the base we choose.

        Set<TournamentParticipant> prevParticipantsPre = new HashSet<>(prev.getTournamentParticipants());
        Set<TournamentParticipant> v1ParticipantsPre = new HashSet<>(v1.getTournamentParticipants());
        Set<TournamentParticipant> v2ParticipantsPre = new HashSet<>(v2.getTournamentParticipants());

        TournamentParticipant.syncParticipantsVersions(prevParticipantsPre, v1ParticipantsPre, v2ParticipantsPre, prev.getTournamentCourseExecution().getCourseExecutionVersion(), v1.getTournamentCourseExecution().getCourseExecutionVersion(), v2.getTournamentCourseExecution().getCourseExecutionAggregateId());

        Set<TournamentParticipant> prevParticipants = new HashSet<>(prevParticipantsPre);
        Set<TournamentParticipant> v1Participants = new HashSet<>(v1ParticipantsPre);
        Set<TournamentParticipant> v2Participants = new HashSet<>(v2ParticipantsPre);


        Set<TournamentParticipant> addedParticipants =  SetUtils.union(
                SetUtils.difference(v1Participants, prevParticipants),
                SetUtils.difference(v2Participants, prevParticipants)
        );

        Set<TournamentParticipant> removedParticipants = SetUtils.union(
                SetUtils.difference(prevParticipants, v1Participants),
                SetUtils.difference(prevParticipants, v2Participants)
        );

        Set<TournamentParticipant> mergedParticipants = SetUtils.union(SetUtils.difference(prevParticipants, removedParticipants), addedParticipants);
        mergedTournament.setTournamentParticipants(mergedParticipants.stream().map(TournamentParticipant::new).collect(Collectors.toSet()));

    }

    private void mergeTopics(Set<String> toCommitVersionChangedFields, Tournament committedTournament, Tournament mergedTournament) {
        if (toCommitVersionChangedFields.contains("tournamentTopics")) {
            mergedTournament.setTournamentTopics(getTournamentTopics().stream().map(TournamentTopic::new).collect(Collectors.toSet()));
        } else {
            mergedTournament.setTournamentTopics(committedTournament.getTournamentTopics().stream().map(TournamentTopic::new).collect(Collectors.toSet()));
        }
    }

}
