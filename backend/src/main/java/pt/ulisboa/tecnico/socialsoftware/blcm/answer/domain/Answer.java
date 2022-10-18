package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.ANSWER;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.REMOVE_USER;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.QUESTION_ALREADY_ANSWERED;

/*
    INTRA-INVARIANTS:
        FINAL_ANSWER_DATE
        FINAL_CREATION_DATE
        FINAL_USER
        FINAL_QUIZ
    INTER-INVARIANTS:
        USER_EXISTS
        QUIZ_EXISTS
        QUESTION_EXISTS
        QUIZ_COURSE_EXECUTION_SAME_AS_QUESTION_COURSE
        QUIZ_COURSE_EXECUTION_SAME_AS_USER

 */
@Entity
@Table(name = "answers")
public class Answer extends Aggregate {
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

    public Answer() { }

    public Answer(Integer aggregateId, AnswerUser answerUser, AnswerQuiz answerQuiz) {
        super(aggregateId, ANSWER);
        setUser(answerUser);
        setQuiz(answerQuiz);
    }

    public Answer(Answer other) {
        super(other.getAggregateId(), ANSWER);
        setId(null);
        setUser(other.getUser());
        setQuiz(other.getQuiz());
        setAnswerDate(other.getAnswerDate());
        setCreationDate(other.getCreationDate());
        setQuestionAnswers(new ArrayList<>());
        setProcessedEvents(new HashMap<>(other.getProcessedEvents()));
        setEmittedEvents(new HashMap<>(other.getEmittedEvents()));
        setPrev(other);
    }



    @Override
    public void verifyInvariants() {

    }

    @Override
    public Set<String> getEventSubscriptions() {
        return Set.of(REMOVE_USER);
    }

    @Override
    public Set<String> getFieldsChangedByFunctionalities() {
        return Set.of("questionAnswers", "answerDate");
    }

    @Override
    public Set<String[]> getIntentions() {
        return new HashSet<>();
    }

    @Override
    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields) {
        return null;
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

    public QuestionAnswer findQuestionAnswer(Integer questionAggregateId) {
        for(QuestionAnswer qa : this.questionAnswers) {
            if(qa.getQuestionAggregateId().equals(questionAggregateId)) {
                return qa;
            }
        }
        return null;
    }
}
