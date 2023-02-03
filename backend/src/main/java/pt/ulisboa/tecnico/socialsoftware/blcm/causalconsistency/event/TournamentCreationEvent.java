package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;

/* EXISTS FOR EXAMPLE ONLY. MAY NEED TO DELETE LATER*/
@Entity
public class TournamentCreationEvent extends Event {
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer numberOfQuestions;

    public TournamentCreationEvent() {
        super();
    }
    public TournamentCreationEvent(Tournament tournament) {
        super(tournament.getAggregateId());
        setStartTime(tournament.getStartTime());
        setEndTime(tournament.getEndTime());
        setNumberOfQuestions(tournament.getNumberOfQuestions());
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }
}
