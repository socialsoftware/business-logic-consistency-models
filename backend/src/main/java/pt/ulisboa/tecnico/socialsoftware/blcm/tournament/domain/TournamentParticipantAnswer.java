package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import jakarta.persistence.*;

@Embeddable
public class TournamentParticipantAnswer {
    private Integer answerAggregateId;
    private Integer answerVersion;
    private Integer numberOfAnswered;
    private Integer numberOfCorrect;
    @Column
    private boolean answered;

    public TournamentParticipantAnswer() {
        setNumberOfAnswered(0);
        setNumberOfCorrect(0);
        setAnswerVersion(0);
        setAnswered(false);
    }

    public TournamentParticipantAnswer(TournamentParticipantAnswer other) {
        setAnswerAggregateId(other.getAnswerAggregateId());
        setNumberOfAnswered(other.getNumberOfAnswered());
        setNumberOfCorrect(other.getNumberOfCorrect());
        setAnswerVersion(other.getAnswerVersion());
        setAnswered(other.isAnswered());
    }


    public Integer getAnswerAggregateId() {
        return answerAggregateId;
    }

    public void setAnswerAggregateId(Integer id) {
        this.answerAggregateId = id;
    }

    public Integer getAnswerVersion() {
        return answerVersion;
    }

    public void setAnswerVersion(Integer answerVersion) {
        this.answerVersion = answerVersion;
    }

    public Integer getNumberOfAnswered() {
        return numberOfAnswered;
    }

    public void setNumberOfAnswered(Integer numberOfAnswered) {
        this.numberOfAnswered = numberOfAnswered;
    }

    public Integer getNumberOfCorrect() {
        return numberOfCorrect;
    }

    public void setNumberOfCorrect(Integer numberOfCorrect) {
        this.numberOfCorrect = numberOfCorrect;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public void incrementAnswered() {
        this.numberOfAnswered++;
    }

    public void incrementCorrect() {
        this.numberOfCorrect++;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TournamentParticipantAnswer)) {
            return false;
        }
        TournamentParticipantAnswer answer = (TournamentParticipantAnswer) obj;
        return getAnswerAggregateId() != null && getAnswerAggregateId().equals(answer.getAnswerAggregateId()) &&
                getAnswerVersion() != null && getAnswerVersion().equals(answer.getAnswerVersion());
    }
}
