package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tournament_processed_anonymize_user_events")
public class ProcessedAnonymizeUserEvents {
    @Id
    private Integer id;

    @Column
    private Integer lastProcessed;

    public ProcessedAnonymizeUserEvents() {

    }

    public ProcessedAnonymizeUserEvents(Integer lastProcessed) {
        this.lastProcessed = lastProcessed;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLastProcessed() {
        return lastProcessed;
    }

    public void setLastProcessed(Integer lastProcessed) {
        this.lastProcessed = lastProcessed;
    }
}
