package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuestionAnswerDto;

import javax.persistence.Embeddable;



@Embeddable
public class QuestionAnswer {

    private Option option;

    private Integer questionAggregateId;

    private Integer timeTaken;

    public  QuestionAnswer() {

    }

    public QuestionAnswer(QuestionAnswerDto questionAnswerDto) {
        setOption(new Option(questionAnswerDto.getOption()));
        setQuestionAggregateId(questionAnswerDto.getQuestionAggregateId());
        setTimeTaken(questionAnswerDto.getTimeTaken());
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public Integer getQuestionAggregateId() {
        return questionAggregateId;
    }

    public void setQuestionAggregateId(Integer questionId) {
        this.questionAggregateId = questionId;
    }

    public Integer getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Integer timeTaken) {
        this.timeTaken = timeTaken;
    }
}
