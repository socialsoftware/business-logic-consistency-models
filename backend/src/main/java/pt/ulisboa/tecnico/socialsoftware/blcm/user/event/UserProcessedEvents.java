package pt.ulisboa.tecnico.socialsoftware.blcm.user.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.event.EventType;

import javax.persistence.*;

@Entity
@Table(name = "user_processed_events")
public class UserProcessedEvents {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique = true, name = "event_type")
    private String eventType;

    @Column
    private Integer lastProcessed;

    public UserProcessedEvents() {

    }

    public UserProcessedEvents(Integer lastProcessed, String eventType) {
        this.lastProcessed = lastProcessed;
        this.eventType = eventType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Integer getLastProcessed() {
        return lastProcessed;
    }

    public void setLastProcessed(Integer lastProcessed) {
        this.lastProcessed = lastProcessed;
    }

}
