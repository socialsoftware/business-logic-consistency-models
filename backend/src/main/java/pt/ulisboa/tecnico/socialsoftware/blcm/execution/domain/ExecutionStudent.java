package pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipant;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ExecutionStudent {
    @Column(name = "user_aggregate_id")
    private Integer aggregateId;
    @Column(name = "user_aggregate_version")
    private Integer version;

    private String name;

    private String username;

    private boolean active;

    public ExecutionStudent() {

    }

    public ExecutionStudent(UserDto userDto) {
        setAggregateId(userDto.getAggregateId());
        setVersion(userDto.getVersion());
        setUsername(userDto.getName());
        setUsername(userDto.getUsername());
        setActive(userDto.isActive());
    }

    public void anonymize() {
        setName("ANONYMOUS");
        setUsername("ANONYMOUS");
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer userAggregateId) {
        this.aggregateId = userAggregateId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer userVersion) {
        this.version = userVersion;
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

    public void setUsername(String userName) {
        this.username = userName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UserDto buildDto() {
        UserDto userDto = new UserDto();
        userDto.setAggregateId(getAggregateId());
        userDto.setVersion(getVersion());
        userDto.setName(getName());
        userDto.setUsername(getUsername());
        return userDto;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getAggregateId();
        hash = 31 * hash + (getVersion() == null ? 0 : getVersion().hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TournamentParticipant)) {
            return false;
        }
        TournamentParticipant tournamentParticipant = (TournamentParticipant) obj;
        return getAggregateId() != null && getAggregateId().equals(tournamentParticipant.getAggregateId()) &&
                getVersion() != null && getVersion().equals(tournamentParticipant.getVersion());
    }
}
