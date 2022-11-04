package pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipant;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipantAnswer;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.util.Set;

@Entity
public class ExecutionStudent extends AggregateComponent {

    private String name;

    private String username;

    private boolean active;

    private Aggregate.AggregateState state;

    public ExecutionStudent() {
        super();
    }

    public ExecutionStudent(UserDto userDto) {
        super(userDto.getAggregateId(), userDto.getVersion());
        setName(userDto.getName());
        setUsername(userDto.getUsername());
        setActive(userDto.isActive());
        setState(Aggregate.AggregateState.valueOf(userDto.getState()));
    }

    public ExecutionStudent(ExecutionStudent other) {
        super(other.getAggregateId(), other.getVersion());
        setName(other.getName());
        setUsername(other.getUsername());
        setActive(other.isActive());
        setState(other.getState());
    }

    public void anonymize() {
        setName("ANONYMOUS");
        setUsername("ANONYMOUS");
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

    public static void syncStudentVersions(Set<ExecutionStudent> prevStudents, Set<ExecutionStudent> v1Students, Set<ExecutionStudent> v2Students) {
        for(ExecutionStudent s1 : v1Students) {
            for(ExecutionStudent s2 : v2Students) {
                if(s1.getAggregateId().equals(s2.getAggregateId())) {
                    if(s1.getVersion() > s2.getVersion()) {
                        s2.setVersion(s1.getVersion());
                        s2.setName(s1.getName());
                        s2.setUsername(s1.getUsername());
                    }

                    if(s2.getVersion() > s1.getVersion()) {
                        s1.setVersion(s2.getVersion());
                        s1.setName(s2.getName());
                        s1.setUsername(s2.getUsername());
                    }
                }
            }

            // no need to check again because the prev does not contain any newer version than v1 an v2
            for(ExecutionStudent prevStudent : prevStudents) {
                if(s1.getAggregateId().equals(prevStudent.getAggregateId())) {
                    if(s1.getVersion() > prevStudent.getVersion()) {
                        prevStudent.setVersion(s1.getVersion());
                        prevStudent.setName(s1.getName());
                        prevStudent.setUsername(s1.getUsername());
                    }

                    if(prevStudent.getVersion() > s1.getVersion()) {
                        s1.setVersion(prevStudent.getVersion());
                        s1.setName(prevStudent.getName());
                        s1.setUsername(prevStudent.getUsername());
                    }
                }
            }
        }
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
