package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.EventualConsistencyDependency;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.TOURNAMENT;
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

    @Embedded
    @Column(name = "creator")
    private TournamentCreator creator;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tournament_participants")
    private Set<TournamentParticipant> participants;

    @Embedded
    @Column(name = "course_execution")
    private TournamentCourseExecution courseExecution;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tournament_topics")
    private Set<TournamentTopic> topics;

    @Embedded
    @Column(name = "tournament_quiz")
    private TournamentQuiz quiz;



    public Tournament() {

    }

    // TODO should the version be assigned on the functionality or service?
    public Tournament(Integer aggregateId, TournamentDto tournamentDto, TournamentCreator creator,
                      TournamentCourseExecution execution, Set<TournamentTopic> topics, TournamentQuiz quiz, Integer version) {
        super(aggregateId, TOURNAMENT);
        setStartTime(DateHandler.toLocalDateTime(tournamentDto.getStartTime()));
        setEndTime(DateHandler.toLocalDateTime(tournamentDto.getEndTime()));
        setNumberOfQuestions(tournamentDto.getNumberOfQuestions());
        setCancelled(tournamentDto.isCancelled());
        setCreator(creator);
        setCourseExecution(execution);
        setTopics(topics);
        setQuiz(quiz);
        setCreationTs(LocalDateTime.now());
    }
    /* used to update the tournament by creating new versions */
    public Tournament(Tournament other) {
        super(other.getAggregateId(), TOURNAMENT);
        setId(null); /* to force a new database entry when saving to be able to distinguish between versions of the same aggregate*/
        setStartTime(other.getStartTime());
        setEndTime(other.getEndTime());
        setNumberOfQuestions(other.getNumberOfQuestions());
        setCancelled(other.isCancelled());
        setCourseExecution(other.getCourseExecution());
        setTopics(other.getTopics());
        setQuiz(other.getQuiz());
        setCreator(other.getCreator()); /* change this to create new instances (maye this not)*/
        setParticipants(new HashSet<>(other.getParticipants())); /* change this to create new instances (maybe not needed) */
        setPrev(other);
    }

    public boolean invariantStartTimeBeforeEndTime() {
        return this.startTime.isBefore(this.endTime);
    }
    public boolean invariantUniqueParticipant() {
        return this.participants.size()
                ==
                this.participants.stream()
                .map(TournamentParticipant::getAggregateId)
                .distinct()
                .count();
    }

    public boolean invariantParticipantsEnrolledBeforeStarTime() {
        for(TournamentParticipant p : this.participants) {
            if(p.getEnrollTime().isAfter(this.startTime)) {
                return false;
            }
        }
        return true;
    }
    public boolean invariantAnswerBeforeStart() {
        if(LocalDateTime.now().isBefore(this.startTime)) {
            for(TournamentParticipant t : this.participants) {
                if(t.getAnswer() != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /* ---------- INSERT MORE INVARIANTS ---------- */

    @Override
    public boolean verifyInvariants() {
        if(!(invariantAnswerBeforeStart()
                && invariantUniqueParticipant()
                && invariantParticipantsEnrolledBeforeStarTime()
                && invariantStartTimeBeforeEndTime())) {
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


        if(v1.getState().equals(DELETED)) {
            throw new TutorException(TOURNAMENT_DELETED, v1.getAggregateId());
        }

        if(v2.getState().equals(DELETED)) {
            throw new TutorException(TOURNAMENT_DELETED, v2.getAggregateId());
        }

        Set<String> v1ChangedFields = getChangedFields(prev, v1);
        Set<String> v2ChangedFields = getChangedFields(prev, v2);




        /* take the state into account because we don't want to override a deleted object*/

        if (checkNonIncrementalChanges(v1ChangedFields, v2ChangedFields) || checkNonIncrementalChanges(v2ChangedFields, v1ChangedFields)) {
            throw new TutorException(TOURNAMENT_MERGE_FAILURE, prev.getAggregateId());
        }

        Tournament mergedTournament = new Tournament(v1);


        if(v1ChangedFields.contains("participants") || v2ChangedFields.contains("participants")) {

            Set<TournamentParticipant> addedParticipants =  SetUtils.union(
                    SetUtils.difference(v1.getParticipants(), prev.getParticipants()),
                    SetUtils.difference(v2.getParticipants(), prev.getParticipants())
            );

            Set<TournamentParticipant> removedParticipants = SetUtils.union(
                    SetUtils.difference(prev.getParticipants(), v1.getParticipants()),
                    SetUtils.difference(prev.getParticipants(), v2.getParticipants())
            );
            mergedTournament.setParticipants(SetUtils.union(SetUtils.difference(prev.getParticipants(), removedParticipants), addedParticipants));

        }
        // TODO see explanation for prev assignment in Quiz
        return mergedTournament;
    }

    private static Set<String> getChangedFields(Tournament prev, Tournament v) {
        Set<String> v1ChangedFields = new HashSet<>();
        if(prev.getStartTime() != v.getStartTime()) {
            v1ChangedFields.add("startTime");
        }

        if(prev.getStartTime() != v.getEndTime()) {
            v1ChangedFields.add("endTime");
        }

        if(!prev.getParticipants().equals(v.getParticipants())) {
            v1ChangedFields.add("participants");
        }

        if(!prev.getTopics().equals(v.getTopics())) {
            v1ChangedFields.add("topics");
        }

        if(!prev.getTopics().equals(v.getNumberOfQuestions())) {
            v1ChangedFields.add("numberOfQuestions");
        }

         return v1ChangedFields;
    }

    private static boolean checkNonIncrementalChanges(Set<String> v1ChangedFields, Set<String> v2ChangedFields) {
        if(v1ChangedFields.contains("startTime")
                && (v2ChangedFields.contains("startTime") ||
                v2ChangedFields.contains("endTime") ||
                v2ChangedFields.contains("topics") ||
                v2ChangedFields.contains("numberOfQuestions"))) {

            return true;
        }

        if(v1ChangedFields.contains("endTime")
                && (v2ChangedFields.contains("startTime") ||
                v2ChangedFields.contains("endTime") ||
                v2ChangedFields.contains("topics") ||
                v2ChangedFields.contains("numberOfQuestions"))) {

            return true;
        }

        if(v1ChangedFields.contains("topics")
                && (v2ChangedFields.contains("startTime") ||
                v2ChangedFields.contains("endTime") ||
                v2ChangedFields.contains("topics") ||
                v2ChangedFields.contains("numberOfQuestions"))) {

            return true;
        }

        if(v1ChangedFields.contains("numberOfQuestions")
                && (v2ChangedFields.contains("startTime") ||
                v2ChangedFields.contains("endTime") ||
                v2ChangedFields.contains("topics") ||
                v2ChangedFields.contains("numberOfQuestions"))) {

            return true;
        }
        return false;
    }


    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public Map<Integer, EventualConsistencyDependency> getDependenciesMap() {
        Map<Integer , EventualConsistencyDependency> depMap = new HashMap<>();
        depMap.put(this.courseExecution.getAggregateId(), new EventualConsistencyDependency(this.courseExecution.getAggregateId(), AggregateType.COURSE_EXECUTION ,this.courseExecution.getVersion()));
        this.participants.forEach(p -> {
            depMap.put(p.getAggregateId(), new EventualConsistencyDependency(this.courseExecution.getAggregateId(), AggregateType.USER, p.getVersion()));
        });
        depMap.put(this.creator.getAggregateId(), new EventualConsistencyDependency(this.creator.getAggregateId(), AggregateType.USER ,this.creator.getVersion()));
        depMap.put(this.quiz.getAggregateId(), new EventualConsistencyDependency(this.quiz.getAggregateId(), AggregateType.QUIZ ,this.quiz.getVersion()));

        return depMap;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public TournamentCreator getCreator() {
        return creator;
    }

    public void setCreator(TournamentCreator creator) {
        this.creator = creator;
    }

    public Set<TournamentParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<TournamentParticipant> participants) {
        this.participants = participants;
    }

    public void addParticipant(TournamentParticipant participant) {
        this.participants.add(participant);
    }

    public TournamentCourseExecution getCourseExecution() {
        return courseExecution;
    }

    public void setCourseExecution(TournamentCourseExecution courseExecution) {
        this.courseExecution = courseExecution;
    }

    public Set<TournamentTopic> getTopics() {
        return topics;
    }

    public void setTopics(Set<TournamentTopic> topics) {
        this.topics = topics;
    }

    public TournamentQuiz getQuiz() {
        return quiz;
    }

    public void setQuiz(TournamentQuiz tournamentQuiz) {
        this.quiz = tournamentQuiz;
    }

    public TournamentParticipant findParticipant(Integer userAggregateId) {
        return this.participants.stream().filter(p -> p.getAggregateId() == userAggregateId).findFirst()
                .orElseThrow(() -> new TutorException(TOURNAMENT_PARTICIPANT_NOT_FOUND, userAggregateId, getAggregateId()));
    }

    public void removeParticipant(TournamentParticipant participant) {
        this.participants.remove(participant);
    }

    @Override
    public void setVersion(Integer version) {
        if(this.quiz.getVersion() == null) {
            this.quiz.setVersion(version);
        }
        super.setVersion(version);
    }
}
