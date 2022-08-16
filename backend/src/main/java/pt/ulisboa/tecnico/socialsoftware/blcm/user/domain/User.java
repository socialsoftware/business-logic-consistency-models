package pt.ulisboa.tecnico.socialsoftware.blcm.user.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends Aggregate {
    @Column
    private String name;

    @Column
    private String username;

    public User() {

    }

    public User(UserDto userDto) {
        setAggregateId(userDto.getAggregateId());
        this.name = userDto.getName();
        this.username = userDto.getUsername();
        setVersion(userDto.getVersion());
    }

    public User(User otherUser) {
        setId(null);
        setAggregateId(otherUser.getAggregateId());
        this.name = otherUser.getName();
        this.username = otherUser.getUsername();
        setState(AggregateState.INACTIVE);
    }

    public static User merge(User prev, User v1, User v2) {
        return null;
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

    @Override
    public boolean verifyInvariants() {
        return false;
    }

    @Override
    public Aggregate getPrev() {
        return null;
    }

    public void setPrev(User user) {

    }
}
