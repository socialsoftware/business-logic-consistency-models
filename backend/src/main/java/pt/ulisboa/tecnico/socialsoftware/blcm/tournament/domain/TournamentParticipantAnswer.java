package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TournamentParticipantAnswer {
    @Column(name = "answer_aggregate_id")
    private Integer aggregateId;
    private Integer version;
    @Column(name = "answer_number_of_answered")
    private Integer numberOfAnswered;
    @Column(name = "answer_number_of_correct")
    private Integer numberOfCorrect;
    @Column
    private boolean answered;

    public TournamentParticipantAnswer() {
        setNumberOfAnswered(0);
        setNumberOfCorrect(0);
        setVersion(0);
        setAnswered(false);
    }

    public TournamentParticipantAnswer(TournamentParticipantAnswer other) {
        setAggregateId(other.getAggregateId());
        setNumberOfAnswered(other.getNumberOfAnswered());
        setNumberOfCorrect(other.getNumberOfCorrect());
        setVersion(other.getVersion());
        setAnswered(other.isAnswered());
    }


    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer id) {
        this.aggregateId = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
        return getAggregateId() != null && getAggregateId().equals(answer.getAggregateId()) &&
                getVersion() != null && getVersion().equals(answer.getVersion());
    }
}
