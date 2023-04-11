package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event.subscribe.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

/* each version of the tournament is a new instance of the tournament*/
/*
    INTRA-INVARIANTS:
    Intra-Invariants (Causal Consistency check on version merge) (Eventual Consistency check on set to APPROVED) (Apply to ACTIVE states)
        CREATOR_IS_FINAL
        COURSE_EXECUTION_IS_FINAL
        QUIZ_IS_FINAL
        START_BEFORE_END_TIME
        UNIQUE_AS_PARTICIPANT
        ENROLL_UNTIL_START_TIME
        ANSWER_BEFORE_START
        FINAL_AFTER_START
        LEAVE_TOURNAMENT
        AFTER_END
        IS_CANCELED
        DELETE
        CREATOR_PARTICIPANT_CONSISTENCY
    INTER-INVARIANTS:
        NUMBER_OF_QUESTIONS
        QUIZ_TOPICS
        START_TIME_AVAILABLE_DATE
        END_TIME_CONCLUSION_DATE
        CREATOR_COURSE_EXECUTION
        PARTICIPANT_COURSE_EXECUTION
        QUIZ_COURSE_EXECUTION
        TOPIC_COURSE_EXECUTION
        QUIZ_QUIZ_ANSWER
        CREATOR_STUDENT
        PARTICIPANT_STUDENT
        NUMBER_OF_ANSWERED
        NUMBER_OF_CORRECT
        INACTIVE_PROPAGATION ????
        CREATOR_EXISTS
        COURSE_EXECUTION_EXISTS
        PARTICIPANT_EXISTS
        TOPIC_EXISTS
        QUIZ_EXISTS
        QUIZ_ANSWER_EXISTS
 */
@Entity
public class Tournament extends Aggregate {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer numberOfQuestions;
    private boolean cancelled;
    /*
    CREATOR_IS_FINAL
		final this.creator.id
     */
    @Embedded
    private TournamentCreator tournamentCreator;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<TournamentParticipant> tournamentParticipants;
    /*
    COURSE_EXECUTION_IS_FINAL
		final this.courseExecution.id
     */
    @Embedded
    private TournamentCourseExecution tournamentCourseExecution;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<TournamentTopic> topics;
    /*
    QUIZ_IS_FINAL
		final this.tournamentQuiz.id
     */
    @Embedded
    private TournamentQuiz tournamentQuiz;

    public Tournament() {
        this.tournamentCreator = null;
        this.tournamentQuiz = null;
        this.tournamentCourseExecution = null;
    }

    public Tournament(Integer aggregateId, TournamentDto tournamentDto, TournamentCreator creator,
                      TournamentCourseExecution execution, Set<TournamentTopic> topics, TournamentQuiz quiz) {
        super(aggregateId, AggregateType.TOURNAMENT);
        setStartTime(DateHandler.toLocalDateTime(tournamentDto.getStartTime()));
        setEndTime(DateHandler.toLocalDateTime(tournamentDto.getEndTime()));
        setNumberOfQuestions(tournamentDto.getNumberOfQuestions());
        setCancelled(tournamentDto.isCancelled());
        this.tournamentCreator = creator;
        setTournamentParticipants(new HashSet<>());
        this.tournamentCourseExecution = execution;
        setTopics(topics);
        this.tournamentQuiz = quiz;
    }
    /* used to update the tournament by creating new versions */
    public Tournament(Tournament other) {
        //super(other.getAggregateId(), TOURNAMENT);
        super(other);
        //setId(null); /* to force a new database entry when saving to be able to distinguish between versions of the same aggregate*/
        setStartTime(other.getStartTime());
        setEndTime(other.getEndTime());
        setNumberOfQuestions(other.getNumberOfQuestions());
        setCancelled(other.isCancelled());
        this.tournamentCourseExecution = new TournamentCourseExecution(other.getTournamentCourseExecution());
        setTopics(new HashSet<>(other.getTopics().stream().map(TournamentTopic::new).collect(Collectors.toSet())));
        this.tournamentQuiz = new TournamentQuiz(other.getTournamentQuiz());
        this.tournamentCreator = new TournamentCreator(other.getTournamentCreator());
        setTournamentParticipants(new HashSet<>(other.getTournamentParticipants().stream().map(TournamentParticipant::new).collect(Collectors.toSet())));
    }

    @Override
    public void remove() {
        /*
        DELETE
		    this.state == DELETED => this.participants.empty
         */
        if (getTournamentParticipants().size() > 0) {
            throw new TutorException(CANNOT_DELETE_TOURNAMENT, getAggregateId());
        }
        super.remove();
    }

    @Override
    public Set<EventSubscription> getEventSubscriptions() {
        Set<EventSubscription> eventSubscriptions = new HashSet<>();
        if (this.getState() == AggregateState.ACTIVE) {
            interInvariantCourseExecutionExists(eventSubscriptions);
            interInvariantCreatorExists(eventSubscriptions);
            interInvariantParticipantExists(eventSubscriptions);
            interInvariantQuizAnswersExist(eventSubscriptions);
            interInvariantTopicsExist(eventSubscriptions);
            interInvariantQuizExists(eventSubscriptions);
        }
        return eventSubscriptions;
    }

    private void interInvariantCourseExecutionExists(Set<EventSubscription> eventSubscriptions) {
        eventSubscriptions.add(new TournamentSubscribesRemoveCourseExecution(this.getTournamentCourseExecution()));
    }

    private void interInvariantCreatorExists(Set<EventSubscription> eventSubscriptions) {
        eventSubscriptions.add(new TournamentSubscribesUnerollStudentFromCourseExecution(this));
        eventSubscriptions.add(new TournamentSubscribesAnonymizeStudent(this));
        eventSubscriptions.add(new TournamentSubscribesUpdateStudentName(this));
    }

    private void interInvariantParticipantExists(Set<EventSubscription> eventSubscriptions) {
        eventSubscriptions.add(new TournamentSubscribesUnerollStudentFromCourseExecution(this));
        eventSubscriptions.add(new TournamentSubscribesAnonymizeStudent(this));
        eventSubscriptions.add(new TournamentSubscribesUpdateStudentName(this));
    }

    private void interInvariantQuizAnswersExist(Set<EventSubscription> eventSubscriptions) {
        for (TournamentParticipant tournamentParticipant: this.tournamentParticipants) {
            if (tournamentParticipant.getParticipantAnswer().getQuizAnswerAggregateId() != null) {
                eventSubscriptions.add(new TournamentSubscribesAnswerQuestion(tournamentParticipant));
            }
        }
    }

    private void interInvariantTopicsExist(Set<EventSubscription> eventSubscriptions) {
        for (TournamentTopic tournamentTopic: this.topics) {
            eventSubscriptions.add(new TournamentSubscribesDeleteTopic(tournamentTopic));
            eventSubscriptions.add(new TournamentSubscribesUpdateTopic(tournamentTopic));
        }
    }

    private void interInvariantQuizExists(Set<EventSubscription> eventSubscriptions) {
        eventSubscriptions.add(new TournamentSubscribesInvalidateQuiz(this.getTournamentQuiz()));
    }

    /* ----------------------------------------- INTRA-AGGREGATE INVARIANTS ----------------------------------------- */

    /*
    START_BEFORE_END_TIME
		this.startTime < this.endTime
     */
    public boolean invariantStartTimeBeforeEndTime() {
        return this.startTime.isBefore(this.endTime);
    }

    /*
    UNIQUE_AS_PARTICIPANT
		p1, p2: this.participants | p1.id != p2.id
     */
    public boolean invariantUniqueParticipant() {
        return this.tournamentParticipants.size()
                ==
                this.tournamentParticipants.stream()
                .map(TournamentParticipant::getParticipantAggregateId)
                .distinct()
                .count();
    }

    /*
    ENROLL_UNTIL_START_TIME
		p : this.participants | p.enrollTime < this.startTime
     */
    public boolean invariantParticipantsEnrolledBeforeStarTime() {
        for (TournamentParticipant p : this.tournamentParticipants) {
            if (p.getEnrollTime().isAfter(this.startTime)) {
                return false;
            }
        }
        return true;
    }

    /*
    ANSWER_BEFORE_START
		now < this.startTime => p: this.participant | p.answer.isEmpty
     */
    public boolean invariantAnswerBeforeStart() {
        if (LocalDateTime.now().isBefore(this.startTime)) {
            for (TournamentParticipant t : this.tournamentParticipants) {
                if (t.getParticipantAnswer().getQuizAnswerAggregateId() != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
    DELETE
		this.state == DELETED => this.participants.empty
     */
    private boolean invariantDeleteCondition() {
        if (getState() == AggregateState.DELETED) {
            return getTournamentParticipants().size() == 0;
        }
        return true;
    }

    /*
        CREATOR_PARTICIPANT_CONSISTENCY
     */

    private boolean invariantCreatorParticipantConsistency() {
        for (TournamentParticipant participant : this.tournamentParticipants) {
            if (participant.getParticipantAggregateId().equals(this.tournamentCreator.getCreatorAggregateId())) {
                if (!participant.getParticipantVersion().equals(this.tournamentCreator.getCreatorVersion())
                        || !participant.getParticipantName().equals(this.tournamentCreator.getCreatorName())
                        || !participant.getParticipantUsername().equals(this.tournamentCreator.getCreatorUsername())) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public void verifyInvariants() {
        if (!(invariantAnswerBeforeStart()
                && invariantUniqueParticipant()
                && invariantParticipantsEnrolledBeforeStarTime()
                && invariantStartTimeBeforeEndTime()
                && invariantDeleteCondition()
                && invariantCreatorParticipantConsistency())) {
            throw new TutorException(INVARIANT_BREAK, getAggregateId());
        }
    }

    public Set<String> getFieldsChangedByFunctionalities()  {
        return Set.of("startTime", "endTime", "numberOfQuestions", "topics", "tournamentParticipants", "cancelled", "tournamentCourseExecution", "tournamentCreator");
    }

    public Set<String[]> getIntentions() {
        return Set.of(
                new String[]{"startTime", "endTime"},
                new String[]{"startTime", "numberOfQuestions"},
                new String[]{"endTime", "numberOfQuestions"},
                new String[]{"numberOfQuestions", "topics"});
    }

    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields){
        if (!(committedVersion instanceof Tournament)) {
            throw new TutorException(AGGREGATE_MERGE_FAILURE, getAggregateId());
        }

        Tournament committedTournament = (Tournament) committedVersion;
        Tournament mergedTournament = new Tournament(this);

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
        mergedTournament.setTournamentParticipants(mergedParticipants);

    }

    private void mergeTopics(Set<String> toCommitVersionChangedFields, Tournament committedTournament, Tournament mergedTournament) {
        if(toCommitVersionChangedFields.contains("topics")) {
            mergedTournament.setTopics(getTopics().stream().map(TournamentTopic::new).collect(Collectors.toSet()));
        } else {
            mergedTournament.setTopics(committedTournament.getTopics().stream().map(TournamentTopic::new).collect(Collectors.toSet()));
        }
    }

    public void cancel() {
        this.cancelled = true;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        /*
        FINAL_AFTER_START
		    now > this.startTime => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final canceled
        IS_CANCELED
		    this.canceled => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final this.participants && p: this.participant | final p.answer
         */
        Tournament prev = (Tournament) getPrev();
        if (prev != null) {
            if ((prev.getStartTime() != null && DateHandler.now().isAfter(prev.getStartTime())) || prev.isCancelled()) {
                throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
            }
        }
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {

        return this.endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        /*
        FINAL_AFTER_START
		    now > this.startTime => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final canceled
        IS_CANCELED
		    this.canceled => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final this.participants && p: this.participant | final p.answer
         */
        Tournament prev = (Tournament) getPrev();
        if (prev != null) {
            if ((prev.getStartTime() != null && DateHandler.now().isAfter(prev.getStartTime())) || prev.isCancelled()) {
                throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
            }
        }
        this.endTime = endTime;
    }

    public Integer getNumberOfQuestions() {
        return this.numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        /*
        FINAL_AFTER_START
		    now > this.startTime => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final canceled
        IS_CANCELED
		    this.canceled => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final this.participants && p: this.participant | final p.answer
         */
        Tournament prev = (Tournament) getPrev();
        if (prev != null) {
            if ((prev.getStartTime() != null && LocalDateTime.now().isAfter(prev.getStartTime())) || prev.isCancelled()) {
                throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
            }
        }
        this.numberOfQuestions = numberOfQuestions;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        /*
        FINAL_AFTER_START
		    now > this.startTime => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final canceled
        IS_CANCELED
		    this.canceled => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final this.participants && p: this.participant | final p.answer
         */
        Tournament prev = (Tournament) getPrev();
        if (prev != null) {
            if ((prev.getStartTime() != null && DateHandler.now().isAfter(prev.getStartTime())) || prev.isCancelled()) {
                throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
            }
        }
        this.cancelled = cancelled;
    }

    public TournamentCreator getTournamentCreator() {
        return tournamentCreator;
    }

    public Set<TournamentParticipant> getTournamentParticipants() {
        return tournamentParticipants;
    }

    public void setTournamentParticipants(Set<TournamentParticipant> tournamentParticipants) {
        /*
        IS_CANCELED
		    this.canceled => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final this.participants && p: this.participant | final p.answer
         */
        /*Tournament prev = (Tournament) getPrev();
        if(prev != null && prev.isCancelled()) {
            throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
        }*/
        this.tournamentParticipants = tournamentParticipants;
    }

    public void addParticipant(TournamentParticipant participant) {
        /*
        IS_CANCELED
		    this.canceled => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final this.participants && p: this.participant | final p.answer
         */
        Tournament prev = (Tournament) getPrev();
        if (prev != null && prev.isCancelled()) {
            throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
        }
        this.tournamentParticipants.add(participant);
    }

    public TournamentCourseExecution getTournamentCourseExecution() {
        return this.tournamentCourseExecution;
    }

    public Set<TournamentTopic> getTopics() {
        return topics;
    }

    public void setTopics(Set<TournamentTopic> topics) {
        /*
        FINAL_AFTER_START
		    now > this.startTime => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final canceled
        IS_CANCELED
		    this.canceled => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final this.participants && p: this.participant | final p.answer
         */
        Tournament prev = (Tournament) getPrev();
        if(prev != null) {
            if((prev.getStartTime() != null && LocalDateTime.now().isAfter(prev.getStartTime())) || prev.isCancelled()) {
                throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
            }
        }
        this.topics = topics;
    }

    public TournamentQuiz getTournamentQuiz() {
        return this.tournamentQuiz;
    }

    public TournamentParticipant findParticipant(Integer userAggregateId) {
        return this.tournamentParticipants.stream().filter(p -> p.getParticipantAggregateId().equals(userAggregateId)).findFirst()
                .orElse(null);
    }

    public boolean removeParticipant(TournamentParticipant participant) {
        /*
        LEAVE_TOURNAMENT
		    p: this.participants | p.state == DELETED => p.answer.isEmpty
        AFTER_END
		    now > this.endTime => p: this.participant | final p.answer
		IS_CANCELED
		    this.canceled => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final this.participants && p: this.participant | final p.answer
         */
        Tournament prev = (Tournament) getPrev();
        if(prev != null) {
            if((prev.getStartTime() != null && LocalDateTime.now().isAfter(prev.getStartTime())) || prev.isCancelled()) {
                throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
            }
        }
        return this.tournamentParticipants.remove(participant);
    }

    // this setVersion is special because the quiz is created in the same transaction and we want to have its version upon commit
    @Override
    public void setVersion(Integer version) {
        if(this.tournamentQuiz.getQuizVersion() == null) {
            this.tournamentQuiz.setQuizVersion(version);
        }
        super.setVersion(version);
    }

    public TournamentTopic findTopic(Integer topicAggregateId) {
        return getTopics().stream()
                .filter(t -> topicAggregateId.equals(t.getTopicAggregateId()))
                .findFirst()
                .orElse(null);
    }

    public void removeTopic(TournamentTopic tournamentTopic) {
        this.topics.remove(tournamentTopic);
    }
}
