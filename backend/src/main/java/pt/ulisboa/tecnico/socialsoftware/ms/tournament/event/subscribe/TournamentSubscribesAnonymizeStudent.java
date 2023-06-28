package pt.ulisboa.tecnico.socialsoftware.ms.tournament.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.AnonymizeStudentEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.ms.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.ms.user.dto.UserDto;

public class TournamentSubscribesAnonymizeStudent extends EventSubscription {
    private TournamentDto tournamentDto;

    public TournamentSubscribesAnonymizeStudent(Tournament tournament) {
        super(tournament.getTournamentCourseExecution().getCourseExecutionAggregateId(),
                tournament.getTournamentCourseExecution().getCourseExecutionVersion(),
                AnonymizeStudentEvent.class.getSimpleName());
        tournamentDto = new TournamentDto(tournament);
    }

    public boolean subscribesEvent(Event event) {
         return super.subscribesEvent(event) && checkTournamentInfo((AnonymizeStudentEvent)event);
    }

    private boolean checkTournamentInfo(AnonymizeStudentEvent anonymizeStudentEvent) {
        if (tournamentDto.getCreator().getAggregateId().equals(anonymizeStudentEvent.getStudentAggregateId())) {
            return true;
        }

        for (UserDto tournamentParticipant: tournamentDto.getParticipants()) {
            if (tournamentParticipant.getAggregateId().equals(anonymizeStudentEvent.getStudentAggregateId())) {
                return true;
            }
        }

        return false;
    }

}