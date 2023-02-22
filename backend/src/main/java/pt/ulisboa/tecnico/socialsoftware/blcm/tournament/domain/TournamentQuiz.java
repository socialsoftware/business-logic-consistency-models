package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;

@Embeddable
public class TournamentQuiz {
    private Integer quizAggregateId;
    private Integer quizVersion;

    public TournamentQuiz() {

    }
    public TournamentQuiz(Integer quizAggregateId, Integer quizVersion) {
        setQuizAggregateId(quizAggregateId);
        setQuizVersion(quizVersion);
    }

    public TournamentQuiz(TournamentQuiz other) {
        setQuizAggregateId(other.getQuizAggregateId());
        setQuizVersion(other.getQuizVersion());
    }


    public Integer getQuizAggregateId() {
        return quizAggregateId;
    }

    public void setQuizAggregateId(Integer id) {
        this.quizAggregateId = id;
    }

    public Integer getQuizVersion() {
        return quizVersion;
    }

    public void setQuizVersion(Integer quizVersion) {
        this.quizVersion = quizVersion;
    }

    public QuizDto buildDto() {
        QuizDto quizDto = new QuizDto();
        quizDto.setAggregateId(getQuizAggregateId());
        quizDto.setVersion(quizDto.getVersion());

        return  quizDto;
    }
}
