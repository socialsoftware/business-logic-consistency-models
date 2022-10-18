package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.TOURNAMENT;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.*;
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
        super(other.getAggregateId(), TOURNAMENT);
        setId(null); /* to force a new database entry when saving to be able to distinguish between versions of the same aggregate*/
        setStartTime(other.getStartTime());
        setEndTime(other.getEndTime());
        setNumberOfQuestions(other.getNumberOfQuestions());
        setCancelled(other.isCancelled());
        this.courseExecution = other.getCourseExecution();
        setTopics(new HashSet<>(other.getTopics()));
        this.quiz = other.getQuiz();
        this.creator = other.getCreator();
        setParticipants(new HashSet<>(other.getParticipants())); /* change this to create new instances (maybe not needed) */
        setProcessedEvents(new HashMap<>(other.getProcessedEvents()));
        setEmittedEvents(new HashMap<>(other.getEmittedEvents()));
        setPrev(other);
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
    public Set<String> getEventSubscriptions() {
        return Set.of(ANONYMIZE_EXECUTION_STUDENT, REMOVE_COURSE_EXECUTION, REMOVE_USER, UPDATE_TOPIC, DELETE_TOPIC, ANSWER_QUESTION);
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
    private boolean deleteCondition() {
        if(getState() == DELETED) {
            return getParticipants().size() == 0;
        }
        return true;
    }

    @Override
    public void verifyInvariants() {
        if(!(/*invariantAnswerBeforeStart()
                &&*/ invariantUniqueParticipant()
                && invariantParticipantsEnrolledBeforeStarTime()
                && invariantStartTimeBeforeEndTime()
                && deleteCondition())) {
            throw new TutorException(INVARIANT_BREAK, getAggregateId());
        }
    }

    public Set<String> getFieldsChangedByFunctionalities()  {
        return Set.of("startTime", "endTime", "numberOfQuestions", "topics", "participants");
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

        mergeStartTime(toCommitVersionChangedFields, committedTournament, mergedTournament);
        mergeEndTime(toCommitVersionChangedFields, committedTournament, mergedTournament);
        mergeNumberOfQuestions(toCommitVersionChangedFields, committedTournament, mergedTournament);
        mergeParticipants((Tournament) getPrev(), this, committedTournament, mergedTournament);
        mergeTopics((Tournament) getPrev(), this, committedTournament, mergedTournament);

        return mergedTournament;
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

    private static void mergeParticipants(Tournament prev, Tournament v1, Tournament v2, Tournament mergedTournament) {
        /* Here we "calculate" the result of the incremental fields. This fields will always be the same regardless
        * of the base we choose. */

        Set<TournamentParticipant> prevParticipantsPre = new HashSet<>(prev.getParticipants());
        Set<TournamentParticipant> v1ParticipantsPre = new HashSet<>(v1.getParticipants());
        Set<TournamentParticipant> v2ParticipantsPre = new HashSet<>(v2.getParticipants());

        TournamentParticipant.syncParticipantVersions(prevParticipantsPre, v1ParticipantsPre, v2ParticipantsPre);

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



    private static void mergeTopics(Tournament prev, Tournament v1, Tournament v2, Tournament mergedTournament) {
        /* Here we "calculate" the result of the incremental fields. This fields will always be the same regardless
         * of the base we choose. */

        Set<TournamentTopic> prevTopicsPre = new HashSet<>(prev.getTopics());
        Set<TournamentTopic> v1TopicsPre = new HashSet<>(v1.getTopics());
        Set<TournamentTopic> v2TopicsPre = new HashSet<>(v2.getTopics());

        TournamentTopic.syncTopicVersions(prevTopicsPre, v1TopicsPre, v2TopicsPre);

        Set<TournamentTopic> prevTopics = new HashSet<>(prevTopicsPre);
        Set<TournamentTopic> v1Topics = new HashSet<>(v1TopicsPre);
        Set<TournamentTopic> v2Topics = new HashSet<>(v2TopicsPre);

        Set<TournamentTopic> addedTopics =  SetUtils.union(
                SetUtils.difference(v1Topics, prevTopics),
                SetUtils.difference(v2Topics, prevTopics)
        );

        Set<TournamentTopic> removedTopics = SetUtils.union(
                SetUtils.difference(prevTopics, v1Topics),
                SetUtils.difference(prevTopics, v2Topics)
        );

        Set<TournamentTopic> mergedTopics = SetUtils.union(SetUtils.difference(prevTopics, removedTopics), addedTopics);
        mergedTournament.setTopics(mergedTopics);
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
}
