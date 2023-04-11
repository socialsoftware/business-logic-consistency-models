package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.AnonymizeStudentEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

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