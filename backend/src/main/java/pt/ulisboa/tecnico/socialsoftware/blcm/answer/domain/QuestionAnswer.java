package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuestionAnswerDto;

import javax.persistence.Embeddable;



@Embeddable
public class QuestionAnswer {

    private Integer optionSequenceChoice;

    private Integer questionAggregateId;

    private Integer timeTaken;

    private Integer optionKey;

    public  QuestionAnswer() {

    }

    public QuestionAnswer(QuestionAnswerDto questionAnswerDto) {
        setOptionSequenceChoice(questionAnswerDto.getSequence());
        setQuestionAggregateId(questionAnswerDto.getQuestionAggregateId());
        setTimeTaken(questionAnswerDto.getTimeTaken());
        setOptionKey(questionAnswerDto.getOptionKey());
    }

    public Integer getOptionSequenceChoice() {
        return optionSequenceChoice;
    }

    public void setOptionSequenceChoice(Integer optionSequence) {
        this.optionSequenceChoice = optionSequence;
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

    public Integer getOptionKey() {
        return optionKey;
    }

    public void setOptionKey(Integer optionKey) {
        this.optionKey = optionKey;
    }
}
