package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;



@Embeddable
public class TournamentParticipantQuizAnswer {
    private Integer quizAnswerAggregateId;
    private Integer quizAnswerVersion;
    private boolean answered;
    private Integer numberOfAnswered;
    private Integer numberOfCorrect;

    public TournamentParticipantQuizAnswer() {
        setQuizAnswerVersion(0);
        setAnswered(false);
        this.numberOfAnswered = 0;
        this.numberOfCorrect = 0;
    }

    public TournamentParticipantQuizAnswer(TournamentParticipantQuizAnswer other) {
        setQuizAnswerAggregateId(other.getQuizAnswerAggregateId());
        setQuizAnswerVersion(other.getQuizAnswerVersion());
        setAnswered(other.isAnswered());
        this.numberOfAnswered = other.getNumberOfAnswered();
        this.numberOfCorrect = other.getNumberOfCorrect();
    }


    public void updateAnswerWithQuestion(Integer quizAnswerAggregateId, Integer questionAnswerAggregateId, boolean isCorrect, Integer version) {
        if (!this.answered) {
            this.quizAnswerAggregateId = quizAnswerAggregateId;
            this.answered = true;
        }

        if (!this.quizAnswerAggregateId.equals(quizAnswerAggregateId)) {
            throw new TutorException(ErrorMessage.TOURNAMENT_PARTICIPANT_ADDING_ANSWER_WITH_WRONG_QUIZ_ANSWER_ID, quizAnswerAggregateId);
        }

        this.quizAnswerVersion = version;

        this.numberOfAnswered++;
        if (isCorrect) this.numberOfCorrect++;
    }

    public Integer getQuizAnswerAggregateId() {
        return quizAnswerAggregateId;
    }

    public void setQuizAnswerAggregateId(Integer id) {
        this.quizAnswerAggregateId = id;
    }

    public Integer getQuizAnswerVersion() {
        return quizAnswerVersion;
    }

    public void setQuizAnswerVersion(Integer quizAnswerVersion) {
        this.quizAnswerVersion = quizAnswerVersion;
    }


    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public Integer getNumberOfAnswered() {
        return numberOfAnswered;
    }

    public void setNumberOfAnswered(Integer numberOfAnswered) {
        this.numberOfAnswered = numberOfAnswered;
    }

    public Integer getNumberOfCorrect() {
        return this.numberOfCorrect;
    }

    public void setNumberOfCorrect(Integer numberOfCorrect) {
        this.numberOfCorrect = numberOfCorrect;
    }
}
