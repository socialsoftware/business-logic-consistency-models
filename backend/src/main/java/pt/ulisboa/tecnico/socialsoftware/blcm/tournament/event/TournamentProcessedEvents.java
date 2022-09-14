package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import javax.persistence.*;

@Entity
@Table(name = "tournament_processed_user_events")
public class TournamentProcessedEvents {
    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private Integer lastProcessedEventId;

    public TournamentProcessedEvents() {

    }

    public TournamentProcessedEvents(Integer lastProcessedEventId) {
        this.lastProcessedEventId = lastProcessedEventId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLastProcessedEventId() {
        return lastProcessedEventId;
    }

    public void setLastProcessedEventId(Integer lastProcessed) {
        this.lastProcessedEventId = lastProcessed;
    }
}
