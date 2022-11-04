package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Entity
public class TournamentQuiz extends AggregateComponent {
    public TournamentQuiz() {
        super();
    }
    public TournamentQuiz(Integer aggregateId, Integer version) {
        super(aggregateId, version);
    }

    public TournamentQuiz(TournamentQuiz other) {
        super(other.getAggregateId(), other.getVersion());
    }

    public QuizDto buildDto() {
        QuizDto quizDto = new QuizDto();
        quizDto.setAggregateId(getAggregateId());
        quizDto.setVersion(quizDto.getVersion());

        return  quizDto;
    }
}
