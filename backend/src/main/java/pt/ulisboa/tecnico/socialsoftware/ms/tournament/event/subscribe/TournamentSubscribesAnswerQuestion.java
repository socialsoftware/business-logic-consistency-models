package pt.ulisboa.tecnico.socialsoftware.ms.tournament.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.answer.event.publish.QuizAnswerQuestionAnswerEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.tournament.domain.TournamentParticipant;

public class TournamentSubscribesAnswerQuestion extends EventSubscription {
    Integer studentAggregateId;
    public TournamentSubscribesAnswerQuestion(TournamentParticipant tournamentParticipant) {
        super(tournamentParticipant.getParticipantAnswer().getQuizAnswerAggregateId(),
                tournamentParticipant.getParticipantAnswer().getQuizAnswerVersion(),
                QuizAnswerQuestionAnswerEvent.class.getSimpleName());
        this.studentAggregateId = tournamentParticipant.getParticipantAggregateId();
    }

    public boolean subscribesEvent(Event event) {
         return super.subscribesEvent(event) && checkAnswerInfo((QuizAnswerQuestionAnswerEvent)event);
    }

    private boolean checkAnswerInfo(QuizAnswerQuestionAnswerEvent quizAnswerQuestionAnswerEvent) {
        return studentAggregateId.equals(quizAnswerQuestionAnswerEvent.getStudentAggregateId());
    }

}