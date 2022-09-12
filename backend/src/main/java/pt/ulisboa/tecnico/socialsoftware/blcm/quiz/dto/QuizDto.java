package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto;

import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.Quiz;

import java.io.Serializable;
import java.util.List;

public class QuizDto implements Serializable {
    private Integer aggregateId;

    private String title;

    private String availableDate;

    private String conclusionDate;

    private String resultsDate;

    private Integer version;

    private List<Integer> questionsAggregateIds;


    public QuizDto() {

    }

    public QuizDto(Quiz quiz) {

    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(String availableDate) {
        this.availableDate = availableDate;
    }

    public String getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(String conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public String getResultsDate() {
        return resultsDate;
    }

    public void setResultsDate(String resultsDate) {
        this.resultsDate = resultsDate;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<Integer> getQuestionsAggregateIds() {
        return questionsAggregateIds;
    }

    public void setQuestionsAggregateIds(List<Integer> questionsAggregateIds) {
        this.questionsAggregateIds = questionsAggregateIds;
    }
}
