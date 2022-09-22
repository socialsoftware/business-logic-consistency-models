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

    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Integer> processedEventsIds;

    @Column(name = "event_type")
    private String eventType;

    public TournamentProcessedEvents() {

    }

    public TournamentProcessedEvents(String eventType) {
        this.processedEventsIds = new HashSet<>();
        setEventType(eventType);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<Integer> getProcessedEventsIds() {
        return processedEventsIds;
    }

    public void addProcessedEventsIds(Set<Integer> processedEventsIds) {
        this.processedEventsIds.addAll(processedEventsIds);
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
