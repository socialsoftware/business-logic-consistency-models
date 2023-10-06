package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.tournament.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.quiz.event.publish.InvalidateQuizEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.tournament.aggregate.TournamentQuiz;

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