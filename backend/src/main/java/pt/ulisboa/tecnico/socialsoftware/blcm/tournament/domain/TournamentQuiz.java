package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TournamentQuiz {
    @Column(name = "quiz_aggregate_id")
    private Integer aggregateId;

    public TournamentQuiz(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public TournamentQuiz() {

    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer id) {
        this.aggregateId = id;
    }
}
