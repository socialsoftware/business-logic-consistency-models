package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;

/* EXISTS FOR EXAMPLE ONLY. MAY NEED TO DELETE LATER*/
@Entity
@DiscriminatorValue("CREATE_TOURNAMENT")
public class TournamentCreationEvent extends DomainEvent {
    private Integer tournamentId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer numberOfQuestions;

    public TournamentCreationEvent() {
        super();
    }
    public TournamentCreationEvent(Tournament tournament) {
        super();
        this.tournamentId = tournament.getId();
        this.startTime = tournament.getStartTime();
        this.endTime = tournament.getEndTime();
        this.numberOfQuestions = tournament.getNumberOfQuestions();
    }

    public Integer getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Integer tournamentId) {
        this.tournamentId = tournamentId;
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
