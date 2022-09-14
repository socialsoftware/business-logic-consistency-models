package pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.OptionDto;

import java.io.Serializable;

public class QuestionAnswerDto implements Serializable {
    private OptionDto option;

    private Integer questionAggregateId;

    private Integer timeTaken;

    public QuestionAnswerDto() {

    }

    public OptionDto getOption() {
        return option;
    }

    public void setOption(OptionDto option) {
        this.option = option;
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
}
