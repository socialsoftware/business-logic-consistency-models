package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import java.io.Serializable;
import java.util.Set;

public class TournamentDto implements Serializable {
    private Integer aggregateId;

    private Integer version;

    private String startTime;

    private String endTime;

    private Integer numberOfQuestions;

    private boolean isCancelled;

    private TournamentCreator creator;

    private Set<TournamentParticipant> participants;

    private TournamentCourseExecution courseExecution;

    private Set<TournamentTopic> topics;

    private TournamentQuiz quiz;

    public TournamentDto() {

    }

    public TournamentDto(Tournament tournament) {
        setAggregateId(tournament.getAggregateId());
        setVersion(tournament.getVersion());
        setStartTime(DateHandler.toISOString(tournament.getStartTime()));
        setEndTime(DateHandler.toISOString(tournament.getEndTime()));
        setNumberOfQuestions(tournament.getNumberOfQuestions());
        setCancelled(tournament.isCancelled());
        setCreator(tournament.getCreator());
        setParticipants(tournament.getParticipants());
        setCourseExecution(tournament.getCourseExecution());
        setTopics(tournament.getTopics());
        setQuiz(tournament.getQuiz());
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
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

    public void setQuiz(TournamentQuiz quiz) {
        this.quiz = quiz;
    }
}
