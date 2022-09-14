package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;


import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.OptionDto;

import javax.persistence.Embeddable;

@Embeddable
public class Option {
    private Integer sequence;

    private boolean correct;

    private String content;

    public Option(OptionDto optionDto) {
        setSequence(optionDto.getSequence());
        setCorrect(optionDto.isCorrect());
        setContent(optionDto.getContent());
    }

    public Option() {

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
