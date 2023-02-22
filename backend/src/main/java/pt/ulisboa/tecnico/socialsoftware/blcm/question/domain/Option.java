package pt.ulisboa.tecnico.socialsoftware.blcm.question.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.OptionDto;

@Embeddable
public class Option {
    private Integer optionKey;
    private Integer sequence;
    private boolean correct;
    private String content;

    public Option() {

    }

    public Option(OptionDto optionDto) {
        setSequence(optionDto.getSequence());
        setCorrect(optionDto.isCorrect());
        setContent(optionDto.getContent());
    }

    public Option(Option other) {
        setSequence(other.getSequence());
        setCorrect(other.isCorrect());
        setContent(other.getContent());
    }


    public Integer getOptionKey() {
        return optionKey;
    }

    public void setOptionKey(Integer optionKey) {
        this.optionKey = optionKey;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
