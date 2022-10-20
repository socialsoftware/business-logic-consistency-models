package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;

import javax.persistence.Embeddable;



@Embeddable
public class QuestionAnswer {

    private Integer optionSequenceChoice;

    private Integer questionAggregateId;

    private Integer version;

    private Integer timeTaken;

    private Integer optionKey;

    private boolean correct;

    private Aggregate.AggregateState state;

    public  QuestionAnswer() {

    }

    public QuestionAnswer(QuestionAnswerDto questionAnswerDto) {
        setOptionSequenceChoice(questionAnswerDto.getSequence());
        setQuestionAggregateId(questionAnswerDto.getQuestionAggregateId());
        //setTimeTaken(questionAnswerDto.getTimeTaken());
        setOptionKey(questionAnswerDto.getOptionKey());
        setState(Aggregate.AggregateState.ACTIVE);
    }

    public QuestionAnswer(QuestionAnswerDto questionAnswerDto, QuestionDto questionDto) {
        setQuestionAggregateId(questionAnswerDto.getQuestionAggregateId());
        setVersion(questionDto.getVersion());
        setOptionSequenceChoice(questionAnswerDto.getSequence());
        //setTimeTaken(questionAnswerDto.getTimeTaken());
        setOptionKey(questionAnswerDto.getOptionKey());

        if(getOptionKey() < 1 || getOptionKey() > questionDto.getOptionDtos().size()) {
            throw new TutorException(ErrorMessage.INVALID_OPTION_SELECTED, getOptionKey(), getQuestionAggregateId());
        }

        for(OptionDto o : questionDto.getOptionDtos()) {
            if(o.getKey().equals(questionAnswerDto.getOptionKey())) {
                setCorrect(o.isCorrect());
            }
        }
        setState(Aggregate.AggregateState.ACTIVE);
    }

    public QuestionAnswer(QuestionAnswer other) {
        setQuestionAggregateId(other.getQuestionAggregateId());
        setVersion(other.getVersion());
        setOptionSequenceChoice(other.getOptionSequenceChoice());
        setOptionKey(other.getOptionKey());
        setCorrect(other.isCorrect());
        setState(other.getState());
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer questionVersion) {
        this.version = questionVersion;
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

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public Aggregate.AggregateState getState() {
        return state;
    }

    public void setState(Aggregate.AggregateState state) {
        this.state = state;
    }
}
