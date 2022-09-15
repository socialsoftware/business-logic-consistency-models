package pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.OptionDto;

import java.io.Serializable;

public class QuestionAnswerDto implements Serializable {
    private Integer sequence;

    private Integer questionAggregateId;

    private Integer timeTaken;

    private Integer optionKey;

    public QuestionAnswerDto() {

    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Integer getQuestionAggregateId() {
        return questionAggregateId;
    }

    public void setQuestionAggregateId(Integer questionAggregateId) {
        this.questionAggregateId = questionAggregateId;
    }

    public Integer getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Integer timeTaken) {
        this.timeTaken = timeTaken;
    }

    public Integer getOptionKey() {
        return optionKey;
    }

    public void setOptionKey(Integer optionKey) {
        this.optionKey = optionKey;
    }
}
