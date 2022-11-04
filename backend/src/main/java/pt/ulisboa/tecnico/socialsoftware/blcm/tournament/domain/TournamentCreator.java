package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;


@Entity
public class TournamentCreator extends AggregateComponent {
    @Column(name = "creator_name")
    private String name;

    @Column(name = "creator_username")
    private String username;

    @Column(name = "creator_state")
    private Aggregate.AggregateState state;

    public TournamentCreator() {

    }
    public TournamentCreator(Integer aggregateId, String name, String username, Integer version) {
        super(aggregateId, version);
        setName(name);
        setUsername(username);
        setVersion(version);
    }

    public TournamentCreator(TournamentCreator other) {
        super(other.getAggregateId(), other.getVersion());
        setName(other.getName());
        setUsername(other.getUsername());
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
