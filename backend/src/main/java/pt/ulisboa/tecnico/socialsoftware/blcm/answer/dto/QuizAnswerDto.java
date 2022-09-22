package pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.Answer;

import java.io.Serializable;

public class QuizAnswerDto implements Serializable {
    private Integer aggregateId;

    private Integer version;

    private String answerDate;

    private boolean completed;

    private Integer userAggregateId;

    private Integer quizAggregateId;

    //private List<QuestionAnswer> questionAnswers;

    public QuizAnswerDto(Answer answer) {
        setAggregateId(answer.getAggregateId());
        setVersion(answer.getVersion());
        setAnswerDate(answer.getCreationDate().toString());
        setCompleted(answer.isCompleted());
        setUserAggregateId(answer.getUser().getAggregateId());
        setQuizAggregateId(answer.getQuiz().getAggregateId());
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

    public String getAnswerDate() {
        return answerDate;
    }

    public void setAnswerDate(String answerDate) {
        this.answerDate = answerDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Integer getUserAggregateId() {
        return userAggregateId;
    }

    public void setUserAggregateId(Integer userAggregateId) {
        this.userAggregateId = userAggregateId;
    }

    public Integer getQuizAggregateId() {
        return quizAggregateId;
    }

    public void setQuizAggregateId(Integer quizAggregateId) {
        this.quizAggregateId = quizAggregateId;
    }
}
