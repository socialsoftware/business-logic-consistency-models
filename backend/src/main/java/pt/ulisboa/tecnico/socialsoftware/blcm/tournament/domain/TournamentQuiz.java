package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TournamentQuiz {
    @Column(name = "quiz_aggregate_id")
    private Integer aggregateId;

    @Column(name = "quiz_version")
    private Integer version;

    public TournamentQuiz(Integer aggregateId, Integer version) {
        setAggregateId(aggregateId);
        setVersion(version);
    }

    public TournamentQuiz() {

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
}
