package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.event.publish.InvalidateQuizEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.tournament.domain.TournamentQuiz;

public class TournamentSubscribesInvalidateQuiz extends EventSubscription {
    public TournamentSubscribesInvalidateQuiz(TournamentQuiz tournamentQuiz) {
        super(tournamentQuiz.getQuizAggregateId(),
                tournamentQuiz.getQuizVersion(),
                InvalidateQuizEvent.class.getSimpleName());
    }

    public boolean subscribesEvent(Event event) {
         return super.subscribesEvent(event);
    }

}