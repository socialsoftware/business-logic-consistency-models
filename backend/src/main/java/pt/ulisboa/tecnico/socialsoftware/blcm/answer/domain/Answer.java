package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.ANSWER;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.*;
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
        super(other);
        setUser(new AnswerUser(other.getUser()));
        setQuiz(new AnswerQuiz(other.getQuiz()));
        setAnswerDate(other.getAnswerDate());
        setCreationDate(other.getCreationDate());
        setQuestionAnswers(other.getQuestionAnswers().stream().map(QuestionAnswer::new).collect(Collectors.toList()));
    }



    @Override
    public void verifyInvariants() {

    }

    @Override
    public Set<EventSubscription> getEventSubscriptions() {
        //return Set.of(REMOVE_USER, UNENROLL_STUDENT, INVALIDATE_QUIZ/*, REMOVE_QUIZ*/);
        // TODO should we add remove quiz???
        Set<EventSubscription> eventSubscriptions = new HashSet<>();
        if(getState() == ACTIVE) {
            interInvariantUserExists(eventSubscriptions);
            interInvariantQuizCourseExecutionSameAsUsers(eventSubscriptions);
        }
        return eventSubscriptions;

    }

    private void interInvariantUserExists(Set<EventSubscription> eventSubscriptions) {
        eventSubscriptions.add(new EventSubscription(this.user.getAggregateId(), this.user.getVersion(), REMOVE_USER, this));
    }

    private void interInvariantQuizCourseExecutionSameAsUsers(Set<EventSubscription> eventSubscriptions) {
        eventSubscriptions.add(new EventSubscription(this.quiz.getCourseExecution().getAggregateId(), this.quiz.getCourseExecution().getVersion(), UNENROLL_STUDENT, this));
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
        Answer mergedAnswer = new Answer(this);
        Answer committedAnswer = (Answer) committedVersion;
        mergeUser(mergedAnswer, committedAnswer);
        mergeQuiz(mergedAnswer, committedAnswer);
        mergeAnswerDate(toCommitVersionChangedFields, mergedAnswer, committedAnswer);
        mergeQuestionAnswers((Answer)getPrev(), this, committedAnswer, mergedAnswer);
        return mergedAnswer;
    }

    private void mergeUser(Answer mergedAnswer, Answer committedAnswer) {
        if(getUser().getVersion() >= committedAnswer.getUser().getVersion()) {
            mergedAnswer.setUser(getUser());
        } else {
            mergedAnswer.setUser(committedAnswer.getUser());
        }
    }

    private void mergeQuiz(Answer mergedAnswer, Answer committedAnswer) {
        if(getQuiz().getVersion() >= committedAnswer.getQuiz().getVersion()) {
            mergedAnswer.setQuiz(getQuiz());
        } else {
            mergedAnswer.setQuiz(committedAnswer.getQuiz());
        }
    }

    private void mergeAnswerDate(Set<String> toCommitVersionChangedFields, Answer mergedAnswer, Answer committedAnswer) {
        if(toCommitVersionChangedFields.contains("answerDate")) {
            mergedAnswer.setAnswerDate(getAnswerDate());
        } else {
            mergedAnswer.setAnswerDate(committedAnswer.getAnswerDate());
        }
    }

    private static void mergeQuestionAnswers(Answer prev, Answer v1, Answer v2, Answer mergedAnswer) {
        /* Here we "calculate" the result of the incremental fields. This fields will always be the same regardless
         * of the base we choose. */

        Set<QuestionAnswer> prevQuestionAnswersPre = new HashSet<>(prev.getQuestionAnswers());
        Set<QuestionAnswer> v1QuestionAnswersPre = new HashSet<>(v1.getQuestionAnswers());
        Set<QuestionAnswer> v2QuestionAnswersPre = new HashSet<>(v2.getQuestionAnswers());

        /*is this necessary*/
        //QuestionAnswer.syncQuestionVersions(prevQuestionAnswersPre, v1QuestionAnswersPre, v2QuestionAnswersPre);

        Set<QuestionAnswer> prevQuestionAnswers = new HashSet<>(prevQuestionAnswersPre);
        Set<QuestionAnswer> v1QuestionAnswers = new HashSet<>(v1QuestionAnswersPre);
        Set<QuestionAnswer> v2QuestionAnswers = new HashSet<>(v2QuestionAnswersPre);


        Set<QuestionAnswer> addedQuestionAnswers =  SetUtils.union(
                SetUtils.difference(v1QuestionAnswers, prevQuestionAnswers),
                SetUtils.difference(v2QuestionAnswers, prevQuestionAnswers)
        );

        Set<QuestionAnswer> removedQuestionAnswers = SetUtils.union(
                SetUtils.difference(prevQuestionAnswers, v1QuestionAnswers),
                SetUtils.difference(prevQuestionAnswers, v2QuestionAnswers)
        );

        Set<QuestionAnswer> mergedQuestionAnswers = SetUtils.union(SetUtils.difference(prevQuestionAnswers, removedQuestionAnswers), addedQuestionAnswers);
        mergedAnswer.setQuestionAnswers(new ArrayList<>(mergedQuestionAnswers));

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
