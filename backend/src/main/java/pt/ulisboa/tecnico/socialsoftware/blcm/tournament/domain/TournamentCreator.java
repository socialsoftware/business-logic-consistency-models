package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TournamentCreator {
    @Column(name = "creator_aggregate_id")
    private Integer aggregateId;

    @Column(name = "creator_name")
    private String name;

    @Column(name = "creator_username")
    private String username;

    public TournamentCreator() {

    }
    public TournamentCreator(Integer aggregateId, String name, String username) {
        this.aggregateId = aggregateId;
        this.name = name;
        this.username = username;
    }



    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer id) {
        this.aggregateId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
