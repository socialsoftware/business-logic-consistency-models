package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.ANONYMIZE_USER;

@Entity
@DiscriminatorValue(ANONYMIZE_USER)
public class AnonymizeUserEvent extends DomainEvent{
    private Integer userAggregateId;
    private String name;
    private String username;

    public AnonymizeUserEvent() {
        super();
    }

    public AnonymizeUserEvent(Integer userAggregateId, String name, String username) {
        super();
        setUserAggregateId(userAggregateId);
        setName(name);
        setUsername(username);
    }

    public Integer getUserAggregateId() {
        return userAggregateId;
    }

    public void setUserAggregateId(Integer userAggregateId) {
        this.userAggregateId = userAggregateId;
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
