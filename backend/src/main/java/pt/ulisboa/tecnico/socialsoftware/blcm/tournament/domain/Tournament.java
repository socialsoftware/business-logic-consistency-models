package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.TOURNAMENT_DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.TOURNAMENT_MERGE_FAILURE;

/* each version of the tournament is a new instance of the tournament*/
@Entity
@Table(name = "tournaments")
public class Tournament implements Aggregate {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "aggregate_id")
    private Integer aggregateId;
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @Column(name = "version")
    private Integer version;

    /* this refers to the version ts*/
    @Column(name = "creation_ts")
    private  LocalDateTime creationTs;

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
    private TournamentQuiz tournamentQuiz;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private AggregateState state;


    public Tournament() {

    }
    public Tournament(Integer aggregateId, TournamentDto tournamentDto, TournamentCreator creator,
                      TournamentCourseExecution execution, Set<TournamentTopic> topics, TournamentQuiz quiz, Integer version) {
        setAggregateId(aggregateId);
        setStartTime(DateHandler.toLocalDateTime(tournamentDto.getStartTime()));
        setEndTime(DateHandler.toLocalDateTime(tournamentDto.getEndTime()));
        setNumberOfQuestions(tournamentDto.getNumberOfQuestions());
        setCreator(creator);
        setCourseExecution(execution);
        setTopics(topics);
        setTournamentQuiz(quiz);
        setVersion(version);
        this.creationTs = LocalDateTime.now();
        setState(AggregateState.INACTIVE);

    }
    /* used to update the tournament by creating new versions */
    public Tournament(Tournament other) {
        setId(null);
        setAggregateId(other.getAggregateId());
        setStartTime(other.getStartTime());
        setEndTime(other.getEndTime());
        setNumberOfQuestions(other.getNumberOfQuestions());
        setCourseExecution(other.getCourseExecution());
        setTopics(other.getTopics());
        setTournamentQuiz(other.getTournamentQuiz());
        setState(AggregateState.INACTIVE);
        setCreator(other.getCreator()); /* change this to create new instances (maye this not)*/
        setParticipants(other.getParticipants()); /* change this to create new instances (maybe not needed) */
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
        return invariantAnswerBeforeStart()
                && invariantUniqueParticipant()
                && invariantParticipantsEnrolledBeforeStarTime()
                && invariantStartTimeBeforeEndTime();
    }


    public static Tournament merge(Tournament prev, Tournament v1, Tournament v2) {
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
        if(v1.getState().equals(DELETED)) {
            throw new TutorException(TOURNAMENT_DELETED, v1.getAggregateId());
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


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
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

    public TournamentQuiz getTournamentQuiz() {
        return tournamentQuiz;
    }

    public void setTournamentQuiz(TournamentQuiz tournamentQuiz) {
        this.tournamentQuiz = tournamentQuiz;
    }

    public Integer getVersion() {
        return version;
    }


    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public void setCreationTs(LocalDateTime creationTs) {
        this.creationTs = creationTs;
    }

    public LocalDateTime getCreationTs() {
        return creationTs;
    }

    @Override
    public AggregateState getState() {
        return this.state;
    }

    public void setState(AggregateState state) {
        this.state = state;
    }


}
