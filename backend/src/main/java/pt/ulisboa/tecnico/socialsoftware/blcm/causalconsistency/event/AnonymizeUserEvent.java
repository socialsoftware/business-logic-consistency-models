package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.ANONYMIZE_USER;

@Entity
@DiscriminatorValue(ANONYMIZE_USER)
public class AnonymizeUserEvent extends Event {
    private String name;
    private String username;

    public AnonymizeUserEvent() {
        super();
    }

    public AnonymizeUserEvent(Integer aggregateId, String name, String username) {
        super(aggregateId);
        setAggregateId(aggregateId);
        setName(name);
        setUsername(username);
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
