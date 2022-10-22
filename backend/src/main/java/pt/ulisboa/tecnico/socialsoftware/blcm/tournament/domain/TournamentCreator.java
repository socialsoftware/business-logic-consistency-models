package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

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

    @Column(name = "creator_version")
    private Integer version;

    @Column(name = "creator_state")
    private Aggregate.AggregateState state;

    public TournamentCreator() {

    }
    public TournamentCreator(Integer aggregateId, String name, String username, Integer version) {
        setAggregateId(aggregateId);
        setName(name);
        setUsername(username);
        setVersion(version);
    }

    public TournamentCreator(TournamentCreator other) {
        setAggregateId(other.getAggregateId());
        setName(other.getName());
        setUsername(other.getUsername());
        setVersion(other.getVersion());
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Aggregate.AggregateState getState() {
        return state;
    }

    public void setState(Aggregate.AggregateState state) {
        this.state = state;
    }

    public UserDto buildDto() {
        UserDto userDto = new UserDto();
        userDto.setAggregateId(getAggregateId());
        userDto.setVersion(getVersion());
        userDto.setName(getName());
        userDto.setUsername(getUsername());

        return userDto;
    }
}
