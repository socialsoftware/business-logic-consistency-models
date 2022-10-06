package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tournament_processed_events")
public class TournamentProcessedEvents {
    @Id
    @GeneratedValue
    private Integer id;

    private Integer aggregateId;

    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Integer> processedEventsIds;

    @Column(name = "event_type")
    private String eventType;

    public TournamentProcessedEvents() {

    }

    public TournamentProcessedEvents(String eventType, Integer aggregateId) {
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

    public void addProcessedEventsId(Integer processedEventsIds) {
        this.processedEventsIds.add(processedEventsIds);
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
