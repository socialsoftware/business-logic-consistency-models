package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TournamentParticipantAnswer {
    @Column(name = "answer_aggregate_id")
    private Integer aggregateId;

    @Column(name = "answer_number_of_answered")
    private Integer numberOfAnswered;

    @Column(name = "answer_number_of_correct")
    private Integer numberOfCorrect;

    @Column
    private boolean answered;

    public TournamentParticipantAnswer() {
        setNumberOfAnswered(0);
        setNumberOfCorrect(0);
        setAnswered(false);
    }


    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer id) {
        this.aggregateId = id;
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
}
