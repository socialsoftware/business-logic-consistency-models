package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.TOURNAMENT;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.*;
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
@Table(name = "tournaments")
public class Tournament extends Aggregate {

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @Column
    private boolean cancelled;

    /*
    CREATOR_IS_FINAL
		final this.creator.id
     */
    @Embedded
    @Column(name = "creator")
    private final TournamentCreator creator;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tournament_participants")
    private Set<TournamentParticipant> participants;

    /*
    COURSE_EXECUTION_IS_FINAL
		final this.courseExecution.id
     */
    @Embedded
    @Column(name = "course_execution")
    private final TournamentCourseExecution courseExecution;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tournament_topics")
    private Set<TournamentTopic> topics;

    /*
    QUIZ_IS_FINAL
		final this.tournamentQuiz.id
     */
    @Embedded
    @Column(name = "tournament_quiz")
    private final TournamentQuiz quiz;



    public Tournament() {
        this.creator = null;
        this.quiz = null;
        this.courseExecution = null;
    }

    public Tournament(Integer aggregateId, TournamentDto tournamentDto, TournamentCreator creator,
                      TournamentCourseExecution execution, Set<TournamentTopic> topics, TournamentQuiz quiz) {
        super(aggregateId, TOURNAMENT);
        setStartTime(LocalDateTime.parse(tournamentDto.getStartTime()));
        setEndTime(LocalDateTime.parse(tournamentDto.getEndTime()));
        setNumberOfQuestions(tournamentDto.getNumberOfQuestions());
        setCancelled(tournamentDto.isCancelled());
        this.creator = creator;
        setParticipants(new HashSet<>());
        this.courseExecution = execution;
        setTopics(topics);
        this.quiz = quiz;
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
        this.courseExecution = new TournamentCourseExecution(other.getCourseExecution());
        setTopics(new HashSet<>(other.getTopics().stream().map(TournamentTopic::new).collect(Collectors.toSet())));
        this.quiz = new TournamentQuiz(other.getQuiz());
        this.creator = new TournamentCreator(other.getCreator());
        setParticipants(new HashSet<>(other.getParticipants().stream().map(TournamentParticipant::new).collect(Collectors.toSet())));
    }

    @Override
    public void remove() {
        /*
        DELETE
		    this.state == DELETED => this.participants.empty
         */
        if (getParticipants().size() > 0) {
            throw new TutorException(CANNOT_DELETE_TOURNAMENT, getAggregateId());
        }
        super.remove();
    }

    @Override
    public Set<EventSubscription> getEventSubscriptions() {
        Set<EventSubscription> eventSubscriptions = new HashSet<>();
        if (this.getState() == ACTIVE) {
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
        eventSubscriptions.add(new EventSubscription(this.courseExecution.getAggregateId(), this.courseExecution.getVersion(), REMOVE_COURSE_EXECUTION));
    }

    private void interInvariantCreatorExists(Set<EventSubscription> eventSubscriptions) {
        eventSubscriptions.add(new EventSubscription(this.courseExecution.getAggregateId(), this.courseExecution.getVersion(), UNENROLL_STUDENT, this.creator.getAggregateId()));
        eventSubscriptions.add(new EventSubscription(this.courseExecution.getAggregateId(), this.courseExecution.getVersion(), ANONYMIZE_EXECUTION_STUDENT, this.creator.getAggregateId()));
        eventSubscriptions.add(new EventSubscription(this.courseExecution.getAggregateId(), this.courseExecution.getVersion(), UPDATE_EXECUTION_STUDENT_NAME, this.creator.getAggregateId()));
    }

    private void interInvariantParticipantExists(Set<EventSubscription> eventSubscriptions) {
        for(TournamentParticipant participant : this.participants) {
            eventSubscriptions.add(new EventSubscription(this.courseExecution.getAggregateId(), this.courseExecution.getVersion(), UNENROLL_STUDENT, participant.getAggregateId()));
            eventSubscriptions.add(new EventSubscription(this.courseExecution.getAggregateId(), this.courseExecution.getVersion(), ANONYMIZE_EXECUTION_STUDENT, participant.getAggregateId()));
            eventSubscriptions.add(new EventSubscription(this.courseExecution.getAggregateId(), this.courseExecution.getVersion(), UPDATE_EXECUTION_STUDENT_NAME, participant.getAggregateId()));
        }

    }

    private void interInvariantQuizAnswersExist(Set<EventSubscription> eventSubscriptions) {
        for (TournamentParticipant participant : this.participants) {
            if (participant.getAnswer().getAggregateId() != null) {
                eventSubscriptions.add(new EventSubscription(participant.getAnswer().getAggregateId(), participant.getAnswer().getVersion(), ANSWER_QUESTION));
            }
        }
    }

    private void interInvariantTopicsExist(Set<EventSubscription> eventSubscriptions) {
        for (TournamentTopic topic : this.topics) {
            eventSubscriptions.add(new EventSubscription(topic.getAggregateId(), topic.getVersion(), DELETE_TOPIC));
            eventSubscriptions.add(new EventSubscription(topic.getAggregateId(), topic.getVersion(), UPDATE_TOPIC));
        }
    }

    private void interInvariantQuizExists(Set<EventSubscription> eventSubscriptions) {
        eventSubscriptions.add(new EventSubscription(this.quiz.getAggregateId(), getVersion(), INVALIDATE_QUIZ));
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
        return this.participants.size()
                ==
                this.participants.stream()
                .map(TournamentParticipant::getAggregateId)
                .distinct()
                .count();
    }

    /*
    ENROLL_UNTIL_START_TIME
		p : this.participants | p.enrollTime < this.startTime
     */
    public boolean invariantParticipantsEnrolledBeforeStarTime() {
        for(TournamentParticipant p : this.participants) {
            if(p.getEnrollTime().isAfter(this.startTime)) {
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
        if(LocalDateTime.now().isBefore(this.startTime)) {
            for(TournamentParticipant t : this.participants) {
                if(t.getAnswer().getAggregateId() != null) {
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
        if(getState() == DELETED) {
            return getParticipants().size() == 0;
        }
        return true;
    }

    /*
        CREATOR_PARTICIPANT_CONSISTENCY
     */

    private boolean invariantCreatorParticipantConsistency() {
        for(TournamentParticipant participant : this.participants) {
            if(participant.getAggregateId().equals(this.creator.getAggregateId())) {
                if(!participant.getVersion().equals(this.creator.getVersion())
                        || !participant.getName().equals(this.creator.getName())
                        || !participant.getUsername().equals(this.creator.getUsername())) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public void verifyInvariants() {
        if(!(invariantAnswerBeforeStart()
                && invariantUniqueParticipant()
                && invariantParticipantsEnrolledBeforeStarTime()
                && invariantStartTimeBeforeEndTime()
                && invariantDeleteCondition()
                && invariantCreatorParticipantConsistency())) {
            throw new TutorException(INVARIANT_BREAK, getAggregateId());
        }
    }

    public Set<String> getFieldsChangedByFunctionalities()  {
        return Set.of("startTime", "endTime", "numberOfQuestions", "topics", "participants", "cancelled", "courseExecution");
    }

    public Set<String[]> getIntentions() {
        return Set.of(
                new String[]{"startTime", "endTime"},
                new String[]{"startTime", "numberOfQuestions"},
                new String[]{"endTime", "numberOfQuestions"},
                new String[]{"numberOfQuestions", "topics"});
    }

    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields){
        if(!(committedVersion instanceof Tournament)) {
            throw new TutorException(AGGREGATE_MERGE_FAILURE, getAggregateId());
        }

        Tournament committedTournament = (Tournament) committedVersion;
        Tournament mergedTournament = new Tournament(this);

        // merge of creator is built in the participants dont know yet
        mergeCreator(committedTournament, mergedTournament);
        //mergeCourseExecution(committedTournament, mergedTournament);
        mergeCourseExecution(toCommitVersionChangedFields, committedTournament, mergedTournament);
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
        if(getCourseExecution().getVersion() >= committedTournament.getCourseExecution().getVersion()) {
            mergedTournament.getCreator().setName(getCreator().getName());
            mergedTournament.getCreator().setUsername(getCreator().getUsername());
            mergedTournament.getCreator().setVersion(getCreator().getVersion());
        } else {
            mergedTournament.getCreator().setName(committedTournament.getCreator().getName());
            mergedTournament.getCreator().setUsername(committedTournament.getCreator().getUsername());
            mergedTournament.getCreator().setVersion(committedTournament.getCreator().getVersion());
        }
    }

    /*private void mergeCourseExecution(Tournament committedTournament, Tournament mergedTournament) {
        if(getCourseExecution().getVersion() >= committedTournament.getCourseExecution().getVersion()) {
            mergedTournament.getCourseExecution().setVersion(getCourseExecution().getVersion());
        } else {
            mergedTournament.getCourseExecution().setVersion(committedTournament.getCourseExecution().getVersion());
        }
    }*/

    private void mergeCourseExecution(Set<String> toCommitVersionChangedFields, Tournament committedTournament, Tournament mergedTournament) {
        if(toCommitVersionChangedFields.contains("courseExecution")) {
            mergedTournament.getCourseExecution().setVersion(getCourseExecution().getVersion());
        } else {
            mergedTournament.getCourseExecution().setVersion(committedTournament.getCourseExecution().getVersion());
        }
    }

    private void mergeQuiz(Tournament committedTournament, Tournament mergedTournament) {
        // The quiz aggregate id must be set in case the quiz has been regenerated due to the previous having been invalidated
        if (getQuiz().getVersion() >= committedTournament.getQuiz().getVersion()) {
            mergedTournament.getQuiz().setAggregateId(getQuiz().getAggregateId());
            mergedTournament.getQuiz().setVersion(getQuiz().getVersion());
        } else {
            mergedTournament.getQuiz().setVersion(committedTournament.getQuiz().getAggregateId());
            mergedTournament.getQuiz().setVersion(committedTournament.getQuiz().getVersion());
        }

    }

    private void mergeCancelled(Set<String> toCommitVersionChangedFields, Tournament committedTournament, Tournament mergedTournament) {
        if(toCommitVersionChangedFields.contains("cancelled")) {
            mergedTournament.setCancelled(isCancelled());
        } else {
            mergedTournament.setCancelled(committedTournament.isCancelled());
        }
    }

    private void mergeNumberOfQuestions(Set<String> toCommitVersionChangedFields, Tournament committedTournament, Tournament mergedTournament) {
        if(toCommitVersionChangedFields.contains("numberOfQuestions")) {
            mergedTournament.setNumberOfQuestions(getNumberOfQuestions());
        } else {
            mergedTournament.setNumberOfQuestions(committedTournament.getNumberOfQuestions());
        }
    }

    private void mergeEndTime(Set<String> toCommitVersionChangedFields, Tournament committedTournament, Tournament mergedTournament) {
        if(toCommitVersionChangedFields.contains("endTime")) {
            mergedTournament.setEndTime(getEndTime());
        } else {
            mergedTournament.setEndTime(committedTournament.getEndTime());
        }
    }

    private void mergeStartTime(Set<String> toCommitVersionChangedFields, Tournament committedTournament, Tournament mergedTournament) {
        if(toCommitVersionChangedFields.contains("startTime")) {
            mergedTournament.setStartTime(getStartTime());
        } else {
            mergedTournament.setStartTime(committedTournament.getStartTime());
        }
    }

    private void mergeParticipants(Tournament prev, Tournament v1, Tournament v2, Tournament mergedTournament) {
        /* Here we "calculate" the result of the incremental fields. This fields will always be the same regardless
        * of the base we choose. */

        Set<TournamentParticipant> prevParticipantsPre = new HashSet<>(prev.getParticipants());
        Set<TournamentParticipant> v1ParticipantsPre = new HashSet<>(v1.getParticipants());
        Set<TournamentParticipant> v2ParticipantsPre = new HashSet<>(v2.getParticipants());

        TournamentParticipant.syncParticipantsVersions(prevParticipantsPre, v1ParticipantsPre, v2ParticipantsPre);

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
        mergedTournament.setParticipants(mergedParticipants);

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
        if(prev != null) {
            if((prev.getStartTime() != null && LocalDateTime.now().isAfter(prev.getStartTime())) || prev.isCancelled()) {
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
        if(prev != null) {
            if((prev.getStartTime() != null && LocalDateTime.now().isAfter(prev.getStartTime())) || prev.isCancelled()) {
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
        if(prev != null) {
            if((prev.getStartTime() != null && LocalDateTime.now().isAfter(prev.getStartTime())) || prev.isCancelled()) {
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
        if(prev != null) {
            if((prev.getStartTime() != null && LocalDateTime.now().isAfter(prev.getStartTime())) || prev.isCancelled()) {
                throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
            }
        }
        this.cancelled = cancelled;
    }

    public TournamentCreator getCreator() {
        return creator;
    }

    public Set<TournamentParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<TournamentParticipant> participants) {
        /*
        IS_CANCELED
		    this.canceled => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final this.participants && p: this.participant | final p.answer
         */
        /*Tournament prev = (Tournament) getPrev();
        if(prev != null && prev.isCancelled()) {
            throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
        }*/
        this.participants = participants;
    }

    public void addParticipant(TournamentParticipant participant) {
        /*
        IS_CANCELED
		    this.canceled => final this.startTime && final this.endTime && final this.numberOfQuestions && final this.tournamentTopics && final this.participants && p: this.participant | final p.answer
         */
        Tournament prev = (Tournament) getPrev();
        if(prev != null && prev.isCancelled()) {
            throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
        }
        this.participants.add(participant);
    }

    public TournamentCourseExecution getCourseExecution() {
        return this.courseExecution;
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

    public TournamentQuiz getQuiz() {
        return this.quiz;
    }

    public TournamentParticipant findParticipant(Integer userAggregateId) {
        return this.participants.stream().filter(p -> p.getAggregateId().equals(userAggregateId)).findFirst()
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
        return this.participants.remove(participant);
    }

    // this setVersion is special because the quiz is created in the same transaction and we want to have its version upon commit
    @Override
    public void setVersion(Integer version) {
        if(this.quiz.getVersion() == null) {
            this.quiz.setVersion(version);
        }
        super.setVersion(version);
    }

    public TournamentTopic findTopic(Integer topicAggregateId) {
        return getTopics().stream()
                .filter(t -> topicAggregateId.equals(t.getAggregateId()))
                .findFirst()
                .orElse(null);
    }

    public void removeTopic(TournamentTopic tournamentTopic) {
        this.topics.remove(tournamentTopic);
    }
}
