package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

public class TournamentDto implements Serializable {
    private Integer aggregateId;

    private Integer version;

    private String startTime;

    private String endTime;

    private Integer numberOfQuestions;

    private boolean isCancelled;

    private UserDto creator;

    private Set<UserDto> participants;

    private CourseExecutionDto courseExecution;

    private Set<TopicDto> topics;

    private QuizDto quiz;

    public TournamentDto() {

    }

    public TournamentDto(Tournament tournament) {
        setAggregateId(tournament.getAggregateId());
        setVersion(tournament.getVersion());
        setStartTime(tournament.getStartTime().toString());
        setEndTime(tournament.getEndTime().toString());
        setNumberOfQuestions(tournament.getNumberOfQuestions());
        setCancelled(tournament.isCancelled());
        setCreator(tournament.getCreator().buildDto());
        setParticipants(tournament.getParticipants().stream().map(TournamentParticipant::buildDto).collect(Collectors.toSet()));
        setCourseExecution(tournament.getCourseExecution().buildDto());
        setTopics(tournament.getTopics().stream().map(TournamentTopic::buildDto).collect(Collectors.toSet()));
        setQuiz(tournament.getQuiz().buidlDto());
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

    public UserDto getCreator() {
        return creator;
    }

    public void setCreator(UserDto creator) {
        this.creator = creator;
    }

    public Set<UserDto> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<UserDto> participants) {
        this.participants = participants;
    }

    public CourseExecutionDto getCourseExecution() {
        return courseExecution;
    }

    public void setCourseExecution(CourseExecutionDto courseExecution) {
        this.courseExecution = courseExecution;
    }

    public Set<TopicDto> getTopics() {
        return topics;
    }

    public void setTopics(Set<TopicDto> topics) {
        this.topics = topics;
    }

    public QuizDto getQuiz() {
        return quiz;
    }

    public void setQuiz(QuizDto quiz) {
        this.quiz = quiz;
    }
}
