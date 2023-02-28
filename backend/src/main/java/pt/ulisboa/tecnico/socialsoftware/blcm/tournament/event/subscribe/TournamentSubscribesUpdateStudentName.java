package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

public class TournamentSubscribesUpdateStudentName extends EventSubscription {
    private TournamentDto tournamentDto;

    public TournamentSubscribesUpdateStudentName(Tournament tournament) {
        super(tournament.getTournamentCourseExecution().getCourseExecutionAggregateId(),
                tournament.getTournamentCourseExecution().getCourseExecutionVersion(),
                UpdateStudentNameEvent.class.getSimpleName());
        tournamentDto = new TournamentDto(tournament);
    }

    public boolean subscribesEvent(Event event) {
         return super.subscribesEvent(event) && checkTournamentInfo((UpdateStudentNameEvent)event);
    }

    private boolean checkTournamentInfo(UpdateStudentNameEvent updateStudentNameEvent) {
        if (tournamentDto.getCreator().getAggregateId().equals(updateStudentNameEvent.getStudentAggregateId())) {
            return true;
        }

        for (UserDto tournamentParticipant : tournamentDto.getParticipants()) {
            if (tournamentParticipant.getAggregateId().equals(updateStudentNameEvent.getStudentAggregateId())) {
                return true;
            }
        }

        return false;
    }

}