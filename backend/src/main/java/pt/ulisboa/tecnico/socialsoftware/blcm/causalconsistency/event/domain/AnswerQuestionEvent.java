package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.Answer;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.QuestionAnswer;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.Event;

@Entity
public class AnswerQuestionEvent extends Event {

    private Integer questionAggregateId;

    private Integer quizAggregateId;

    private Integer userAggregateId;

    private boolean correct;

    public AnswerQuestionEvent() {
        super();
    }

    public AnswerQuestionEvent(QuestionAnswer questionAnswer, Answer answer, Integer quizAggregateId) {
        super(answer.getAggregateId());
        setQuestionAggregateId(questionAnswer.getQuestionAggregateId());
        setQuizAggregateId(quizAggregateId);
        setCorrect(questionAnswer.isCorrect());
        setUserAggregateId(answer.getUser().getAggregateId());
        setAggregateId(answer.getAggregateId());
    }

    public Integer getQuestionAggregateId() {
        return questionAggregateId;
    }

    public void setQuestionAggregateId(Integer questionAggregateId) {
        this.questionAggregateId = questionAggregateId;
    }

    public Integer getQuizAggregateId() {
        return quizAggregateId;
    }

    public void setQuizAggregateId(Integer quizAggregateId) {
        this.quizAggregateId = quizAggregateId;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public Integer getUserAggregateId() {
        return userAggregateId;
    }

    public void setUserAggregateId(Integer userAggregateId) {
        this.userAggregateId = userAggregateId;
    }
}
