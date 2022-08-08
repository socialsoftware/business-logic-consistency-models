package pt.ulisboa.tecnico.socialsoftware.blcm.user.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User implements Aggregate {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "aggregate_id")
    private Integer aggregateId;
    @Column
    private Integer version;

    @Column(name = "creation_ts")
    private LocalDateTime creationTs;

    @Column
    private AggregateState state;

    @Column
    private String name;

    @Column
    private String username;

    public User() {

    }

    public User(UserDto userDto) {
        this.aggregateId = userDto.getAggregateId();
        this.name = userDto.getName();
        this.username = userDto.getUsername();
        this.version = userDto.getVersion();
    }

    public User(User otherUser) {
        setId(null);
        this.aggregateId = otherUser.getAggregateId();
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

    @Override
    public Integer getId() {
        return null;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public boolean verifyInvariants() {
        return false;
    }

    @Override
    public LocalDateTime getCreationTs() {
        return this.creationTs;
    }

    @Override
    public void setCreationTs(LocalDateTime time) {
        this.creationTs = time;
    }

    @Override
    public AggregateState getState() {
        return this.state;
    }

    @Override
    public void setState(AggregateState state) {
        this.state = state;
    }

    @Override
    public Integer getAggregateId() {
        return this.aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
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
}
