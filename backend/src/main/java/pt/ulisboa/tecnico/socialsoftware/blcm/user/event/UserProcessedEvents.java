package pt.ulisboa.tecnico.socialsoftware.blcm.user.event;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_processed_events")
public class UserProcessedEvents {
    @Id
    @GeneratedValue
    private Integer id;

    private Integer aggregateId;

    @Column(name = "processed_events_ids")
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Integer> processedEventsIds;

    @Column(name = "event_type")
    private String eventType;

    public UserProcessedEvents() {

    }

    public UserProcessedEvents(String eventType, Integer aggregateId) {
        this.processedEventsIds = new HashSet<>();
        setAggregateId(aggregateId);
        setEventType(eventType);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Set<Integer> getProcessedEventsIds() {
        return processedEventsIds;
    }

    public void addProcessedEventsId(Integer processedEventsId) {
        this.processedEventsIds.add(processedEventsId);
    }

    public boolean containsEvent(Integer eventId) {
        return this.processedEventsIds.contains(eventId);
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
