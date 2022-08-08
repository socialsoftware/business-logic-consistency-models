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
}
