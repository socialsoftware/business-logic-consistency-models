package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "processed_events")
public class ProcessedEvents {
    @Id
    @GeneratedValue
    private Integer id;

    private Integer aggregateId;

    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Integer> processedEventsVersions;

    @Column(name = "event_type")
    private String eventType;

    public ProcessedEvents() {

    }

    public ProcessedEvents(String eventType, Integer aggregateId) {
        this.processedEventsVersions = new HashSet<>();
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

    public Set<Integer> getProcessedEventsVersions() {
        return processedEventsVersions;
    }

    public void addProcessedEventVersion(Integer processedEventsIds) {
        this.processedEventsVersions.add(processedEventsIds);
    }

    public boolean containsEventVersion(Integer eventId) {
        return this.processedEventsVersions.contains(eventId);
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
