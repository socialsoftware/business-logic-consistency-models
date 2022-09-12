package pt.ulisboa.tecnico.socialsoftware.blcm.question.dto;

import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.Option;

public class OptionDto {
    private Integer sequence;

    private boolean correct;

    private String content;

    public OptionDto(Option option) {
        setSequence(option.getSequence());
        setCorrect(option.isCorrect());
        setContent(option.getContent());

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
