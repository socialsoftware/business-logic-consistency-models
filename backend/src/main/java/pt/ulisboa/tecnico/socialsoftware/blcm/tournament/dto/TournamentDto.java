package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.*;

import java.util.Set;

public class TournamentDto {
    private Integer aggregateId;

    private String startTime;

    private String endTime;

    private Integer numberOfQuestions;

    private TournamentCreator creator;

    private Set<TournamentParticipant> participants;

    private TournamentCourseExecution courseExecution;

    private Set<TournamentTopic> topics;

    public TournamentDto(Tournament tournament) {
        this.aggregateId = tournament.getAggregateId();
        this.startTime = startTime;
        this.endTime = endTime;
        this.numberOfQuestions = numberOfQuestions;
        this.creator = creator;
        this.participants = participants;
        this.courseExecution = courseExecution;
        this.topics = topics;
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
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
}
