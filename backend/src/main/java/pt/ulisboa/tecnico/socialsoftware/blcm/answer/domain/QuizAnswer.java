package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.Dependency;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.QUIZ_ANSWER;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.QUESTION_ALREADY_ANSWERED;

@Entity
public class QuizAnswer extends Aggregate {

    @ManyToOne
    private QuizAnswer prev;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "answer_date")
    private LocalDateTime answerDate;

    private boolean completed;

    @Embedded
    private AnswerUser user;

    @Embedded
    private AnswerQuiz quiz;

    @ElementCollection
    private List<QuestionAnswer> questionAnswers;

    public QuizAnswer() { }

    public QuizAnswer(Integer aggregateId, AnswerUser answerUser, AnswerQuiz answerQuiz) {
        super(aggregateId, QUIZ_ANSWER);
        setUser(answerUser);
        setQuiz(answerQuiz);
    }

    public QuizAnswer(QuizAnswer other) {
        super(other.getAggregateId(), QUIZ_ANSWER);
        setId(null);
        setUser(other.getUser());
        setQuiz(other.getQuiz());
        setAnswerDate(other.getAnswerDate());
        setCreationDate(other.getCreationDate());
        setPrev(other);
    }



    @Override
    public boolean verifyInvariants() {
        return true;
    }

    @Override
    public Aggregate getPrev() {
        return null;
    }

    @Override
    public Aggregate merge(Aggregate other) {
        return null;
    }

    @Override
    public Map<Integer, Dependency> getDependenciesMap() {
        return null;
    }

    public void setPrev(QuizAnswer prev) {
        this.prev = prev;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getAnswerDate() {
        return answerDate;
    }

    public void setAnswerDate(LocalDateTime answerDate) {
        this.answerDate = answerDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public AnswerUser getUser() {
        return user;
    }

    public void setUser(AnswerUser user) {
        this.user = user;
    }

    public AnswerQuiz getQuiz() {
        return quiz;
    }

    public void setQuiz(AnswerQuiz quiz) {
        this.quiz = quiz;
    }

    public List<QuestionAnswer> getQuestionAnswers() {
        return questionAnswers;
    }

    public void setQuestionAnswers(List<QuestionAnswer> questionAnswers) {
        this.questionAnswers = questionAnswers;
    }

    public void addQuestionAnswer(QuestionAnswer questionAnswer) {
        List<Integer> answeredQuestionIds = this.questionAnswers.stream()
                .map(QuestionAnswer::getQuestionAggregateId)
                .collect(Collectors.toList());
        if(answeredQuestionIds.contains(questionAnswer.getQuestionAggregateId())) {
            throw new TutorException(QUESTION_ALREADY_ANSWERED, questionAnswer.getQuestionAggregateId(), this.getQuiz().getAggregateId());
        }
        this.questionAnswers.add(questionAnswer);
    }

}
