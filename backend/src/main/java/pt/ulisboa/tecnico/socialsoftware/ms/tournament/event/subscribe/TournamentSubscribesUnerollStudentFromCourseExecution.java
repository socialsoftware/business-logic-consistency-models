package pt.ulisboa.tecnico.socialsoftware.ms.tournament.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.publish.UnerollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.ms.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.ms.user.dto.UserDto;

public class TournamentSubscribesUnerollStudentFromCourseExecution extends EventSubscription {
    TournamentDto tournamentDto;
    public TournamentSubscribesUnerollStudentFromCourseExecution(Tournament tournament) {
        super(tournament.getTournamentCourseExecution().getCourseExecutionAggregateId(),
                tournament.getTournamentCourseExecution().getCourseExecutionVersion(),
                UnerollStudentFromCourseExecutionEvent.class.getSimpleName());
        this.tournamentDto = new TournamentDto(tournament);
    }

    public boolean subscribesEvent(Event event) {
         return super.subscribesEvent(event) && checkTournamentInfo((UnerollStudentFromCourseExecutionEvent)event);
    }

    private boolean checkTournamentInfo(UnerollStudentFromCourseExecutionEvent unerollStudentFromCourseExecutionEvent) {
        if (tournamentDto.getCreator().getAggregateId().equals(unerollStudentFromCourseExecutionEvent.getStudentAggregateId())) {
            return true;
        }

        for (UserDto tournamentParticipant : tournamentDto.getParticipants()) {
            if (tournamentParticipant.getAggregateId().equals(unerollStudentFromCourseExecutionEvent.getStudentAggregateId())) {
                return true;
            }
        }

        return false;
    }

}