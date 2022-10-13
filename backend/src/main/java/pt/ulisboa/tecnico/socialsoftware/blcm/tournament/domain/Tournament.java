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
        return Set.of(ANONYMIZE_USER, REMOVE_COURSE_EXECUTION, REMOVE_USER, UPDATE_TOPIC, DELETE_TOPIC, ANSWER_QUESTION);
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
    public boolean verifyInvariants() {
        if(!(/*invariantAnswerBeforeStart()
                &&*/ invariantUniqueParticipant()
                && invariantParticipantsEnrolledBeforeStarTime()
                && invariantStartTimeBeforeEndTime()
                && deleteCondition())) {
            throw new TutorException(INVARIANT_BREAK, getAggregateId());
        }
        return true;
    }


    @Override
    public Aggregate merge(Aggregate other) {
        /*
        Causal Consistency
		FIELD MERGE RULES
			DEFAULT ATTRIBUTE COMBINATION: NON-INCREMENTAL
			DEFAULT VERSION PICKING PROCESS: LOWER TS
			SPECIFIC ATTRIBUTE COMBINATIONS
				(v1.participants, v2.participants): INCREMENTAL
				(v1.startTime, v2.participants): INCREMENTAL
				(v1.participants, v2.endTime): INCREMENTAL
				p1: v1.participants | p2 = v2.participants | p1.id != p2.id => (p1,p2): INCREMENTAL
				(v1.participants, v2.numberOfQuestions): INCREMENTAL
				(v1.participants, v2.topics): INCREMENTAL
				(v1.participants, v2.endTime): INCREMENTAL
				(v1.numberOfQuestions, v2.numberOfQuestions): INCREMENTAL
		MERGE FUNCTIONS
			participants
				addedParticipants = v1.participants - prev.participants + v2.participants - prev.participants
				removedParticipants = prev.participants - v1.participants + prev.participants - v2.participants
				this.participants = prev.participants - removedParticipants + addedParticipants
			numberOfQuestions
				this.numberOfQuestions = v2.numberOfQuestions (v2 has higher TS)
			*/

        /* if there is an already concurrent version which is deleted this should not execute*/
        Tournament prev = (Tournament) getPrev();
        Tournament v1 = this;
        if(!(other instanceof Tournament)) {
            throw new TutorException(TOURNAMENT_MERGE_FAILURE, getAggregateId());
        }
        Tournament v2 = (Tournament)other;


        if(v1.getState() == DELETED) {
            throw new TutorException(TOURNAMENT_DELETED, v1.getAggregateId());
        }
        /* take the state into account because we don't want to override a deleted object*/

        if(v2.getState() == DELETED) {
            throw new TutorException(TOURNAMENT_DELETED, v2.getAggregateId());
        }

        Set<String> v1ChangedFields = getChangedFields(prev, v1);
        Set<String> v2ChangedFields = getChangedFields(prev, v2);

        /* if updates occur in both versions that change any of the fields in the non-incremental set we stay with
        * concurrent version which was committed first (v2) by throwing an exception on this merge.
        * This implementation does not allow versions to be merged if there are any non-incremental changes it may also
        * exist. SHOULD IT BE THIS WAY??? This requires knowledge of the tournament functionalities: we do not have any
        * functionality which updates both incremental and non-incremental fields at the same time. Update only makes
        * changes to non-incremental fields and addParticipant only makes changes to incremental fields, meaning there
        * is no instance in which we lose an update by aborting the merge. IS IT????? */
        if (checkIntentions(v1ChangedFields, v2ChangedFields) || checkIntentions(v2ChangedFields, v1ChangedFields)) {
            throw new TutorException(TOURNAMENT_MERGE_FAILURE, prev.getAggregateId());
        }

        Tournament mergedTournament;

            /* Here we know that the two updates didn't alter non-incremental fields.
             * We choose the base of the merged version by checking whether the already existing concurrent version (v2) made
             *  changes to non-incremental fields. If it didn't do any changes we choose the version we're trying to commit
             * (v1) as a base. This is because it is the non-incremental fields which determine the generation of the quiz.
             * If v2 did changes to the non-incremental fields the quiz was updated and we want the merged version to have
             * that update. The same thought process for v2. If neither v2 or v1 made changes, it means they both have
             * the same quiz version. This is required because the quiz is a final variable. */
        if(v1ChangedFields.contains("startTime") || v1ChangedFields.contains("endTime") || v1ChangedFields.contains("topics") || v1ChangedFields.contains("numberOfQuestions")) {
            mergedTournament = new Tournament(v1);
        } else {
            mergedTournament = new Tournament(v2);
        }

        mergeTopics(prev, v1, v2, mergedTournament);
        mergeParticipants(prev, v1, v2, mergedTournament);
        // TODO see explanation for prev assignment in Quiz
        mergedTournament.setPrev(getPrev());
        return mergedTournament;
    }

    private static void mergeParticipants(Tournament prev, Tournament v1, Tournament v2, Tournament mergedTournament) {
        /* Here we "calculate" the result of the incremental fields. This fields will always be the same regardless
        * of the base we choose. */

        Set<TournamentParticipant> prevParticipants = new HashSet<>(prev.getParticipants());
        Set<TournamentParticipant> v1Participants = new HashSet<>(v1.getParticipants());
        Set<TournamentParticipant> v2Participants = new HashSet<>(v2.getParticipants());

        for(TournamentParticipant tp1 : v1Participants) {
            for(TournamentParticipant tp2 : v2Participants) {
                if(tp1.getAggregateId().equals(tp2.getAggregateId())) {
                    if(tp1.getVersion() > tp2.getVersion()) {
                        tp2.setVersion(tp1.getVersion());
                        tp2.setName(tp1.getName());
                        tp2.setUsername(tp1.getUsername());
                    }

                    if(tp2.getVersion() > tp1.getVersion()) {
                        tp1.setVersion(tp2.getVersion());
                        tp1.setName(tp2.getName());
                        tp1.setUsername(tp2.getUsername());
                    }
                }
            }

            // no need to check again because the prev does not contain any newer version than v1 an v2
            for(TournamentParticipant tp2 : prevParticipants) {
                if(tp1.getAggregateId().equals(tp2.getAggregateId())) {
                    if(tp1.getVersion() > tp2.getVersion()) {
                        tp2.setVersion(tp1.getVersion());
                        tp2.setName(tp1.getName());
                        tp2.setUsername(tp1.getUsername());
                    }

                    if(tp2.getVersion() > tp1.getVersion()) {
                        tp1.setVersion(tp2.getVersion());
                        tp1.setName(tp2.getName());
                        tp1.setUsername(tp2.getUsername());
                    }
                }
            }
        }

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

        Set<TournamentTopic> prevTopics = new HashSet<>(prev.getTopics());
        Set<TournamentTopic> v1Topics = new HashSet<>(v1.getTopics());
        Set<TournamentTopic> v2Topics = new HashSet<>(v2.getTopics());

        for(TournamentTopic t1 : v1Topics) {
            for(TournamentTopic t2 : v2Topics) {
                if(t1.getAggregateId().equals(t2.getAggregateId())) {
                    if(t1.getVersion() > t2.getVersion()) {
                        t2.setVersion(t1.getVersion());
                        t2.setName(t1.getName());
                    }

                    if(t2.getVersion() > t1.getVersion()) {
                        t1.setVersion(t2.getVersion());
                        t1.setName(t2.getName());
                    }
                }
            }

            // no need to check again because the prev does not contain any newer version than v1 an v2
            for(TournamentTopic tp2 : prevTopics) {
                if(t1.getAggregateId().equals(tp2.getAggregateId())) {
                    if(t1.getVersion() > tp2.getVersion()) {
                        tp2.setVersion(t1.getVersion());
                        tp2.setName(t1.getName());
                    }

                    if(tp2.getVersion() > t1.getVersion()) {
                        t1.setVersion(tp2.getVersion());
                        t1.setName(tp2.getName());
                    }
                }
            }
        }

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

    private static Set<String> getChangedFields(Tournament prev, Tournament v) {
        Set<String> v1ChangedFields = new HashSet<>();
        if(!prev.getStartTime().equals(v.getStartTime())) {
            v1ChangedFields.add("startTime");
        }

        if(!prev.getEndTime().equals(v.getEndTime())) {
            v1ChangedFields.add("endTime");
        }

        if(!prev.getParticipants().equals(v.getParticipants())) {
            v1ChangedFields.add("participants");
        }

        if(!prev.getTopics().equals(v.getTopics())) {
            v1ChangedFields.add("topics");
        }

        if(!prev.getNumberOfQuestions().equals(v.getNumberOfQuestions())) {
            v1ChangedFields.add("numberOfQuestions");
        }

         return v1ChangedFields;
    }

    private static boolean checkIntentions(Set<String> v1ChangedFields, Set<String> v2ChangedFields) {
        if(v1ChangedFields.contains("startTime")
                && (v2ChangedFields.contains("endTime") ||
                v2ChangedFields.contains("topics") ||
                v2ChangedFields.contains("numberOfQuestions"))) {

            return true;
        }

        if(v1ChangedFields.contains("endTime")
                && (v2ChangedFields.contains("startTime") ||
                v2ChangedFields.contains("topics") ||
                v2ChangedFields.contains("numberOfQuestions"))) {

            return true;
        }

        if(v1ChangedFields.contains("topics")
                && (v2ChangedFields.contains("startTime") ||
                v2ChangedFields.contains("endTime") ||
                v2ChangedFields.contains("numberOfQuestions"))) {

            return true;
        }

        if(v1ChangedFields.contains("numberOfQuestions")
                && (v2ChangedFields.contains("startTime") ||
                v2ChangedFields.contains("endTime") ||
                v2ChangedFields.contains("topics"))) {

            return true;
        }
        return false;
    }


    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public Map<Integer, Integer> getSnapshotElements() {
        Map<Integer , Integer> depMap = new HashMap<>();
        depMap.put(this.courseExecution.getAggregateId(), this.courseExecution.getVersion());
        depMap.put(this.creator.getAggregateId(), this.creator.getVersion());
        this.participants.forEach(p -> {
            depMap.put(p.getAggregateId(), p.getVersion());
        });
        depMap.put(this.quiz.getAggregateId(), this.quiz.getVersion());
        return depMap;
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
        Tournament prev = (Tournament) getPrev();
        if(prev != null && prev.isCancelled()) {
            throw new TutorException(CANNOT_UPDATE_TOURNAMENT, getAggregateId());
        }
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
