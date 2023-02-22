package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.dto.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.domain.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.ANSWER;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.QUESTION_ALREADY_ANSWERED;

/*
    INTRA-INVARIANTS:
        FINAL_ANSWER_DATE
        FINAL_CREATION_DATE
        FINAL_USER
        FINAL_QUIZ
        FINAL_COURSE_EXECUTION
    INTER-INVARIANTS:
        USER_EXISTS
        QUIZ_EXISTS
        QUESTION_EXISTS
        COURSE_EXECUTION_EXISTS
        QUESTION_ANSWERS_QUESTION_BELONGS_TO_QUIZ
        QUIZ_COURSE_EXECUTION_SAME_AS_USER_COURSE_EXECUTION
        COURSE_EXECUTION_SAME_AS_USER_COURSE_EXECUTION
        COURSE_EXECUTION_SAME_QUIZ_COURSE_EXECUTION (verified at the service and the quiz doesnt change execution)

 */
@Entity
@Table(name = "answers")
public class Answer extends Aggregate {
    private LocalDateTime creationDate;
    private LocalDateTime answerDate;
    @Column
    private boolean completed;
    @Embedded
    private final AnswerCourseExecution courseExecution;
    @Embedded
    private final AnswerUser user;
    /* it is not final because of the question ids inside*/
    @Embedded
    private AnswerQuiz quiz;

    @ElementCollection
    private List<QuestionAnswer> questionAnswers;

    public Answer() {
        this.courseExecution = new AnswerCourseExecution();
        this.quiz = new AnswerQuiz();
        this.user = new AnswerUser();
    }

    public Answer(Integer aggregateId, AnswerCourseExecution courseExecution, AnswerUser answerUser, AnswerQuiz answerQuiz) {
        super(aggregateId, ANSWER);
        this.courseExecution = courseExecution;
        this.user = answerUser;
        this.quiz = answerQuiz;
    }

    public Answer(Answer other) {
        super(other);
        this.user = new AnswerUser(other.getUser());
        this.quiz = new AnswerQuiz(other.getQuiz());
        this.courseExecution = new AnswerCourseExecution(other.getCourseExecution());
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
        Set<EventSubscription> eventSubscriptions = new HashSet<>();
        if(getState() == ACTIVE) {
            interInvariantCourseExecutionExists(eventSubscriptions);
            interInvariantQuizExists(eventSubscriptions);
            interInvariantUserExists(eventSubscriptions);
        }
        return eventSubscriptions;
    }

    private void interInvariantCourseExecutionExists(Set<EventSubscription> eventSubscriptions) {
        eventSubscriptions.add(new EventSubscription(this.courseExecution.getCourseExecutionAggregateId(), this.courseExecution.getCourseExecutionVersion(), RemoveCourseExecutionEvent.class.getSimpleName(), this));
    }

    private void interInvariantQuizExists(Set<EventSubscription> eventSubscriptions) {
        // also verifies QUESTION_EXISTS because if the question is DELETED the quiz sends this event
        eventSubscriptions.add(new EventSubscription(this.quiz.getQuizAggregateId(), this.quiz.getQuizVersion(), InvalidateQuizEvent.class.getSimpleName(), this));
        // TODO add remove quiz
    }

    private void interInvariantUserExists(Set<EventSubscription> eventSubscriptions) {
        eventSubscriptions.add(new EventSubscription(this.courseExecution.getCourseExecutionAggregateId(), this.courseExecution.getCourseExecutionVersion(), UnerollStudentFromCourseExecutionEvent.class.getSimpleName(), this));
        eventSubscriptions.add(new EventSubscription(this.courseExecution.getCourseExecutionAggregateId(), this.courseExecution.getCourseExecutionVersion(), AnonymizeExecutionStudentEvent.class.getSimpleName(), this));
        eventSubscriptions.add(new EventSubscription(this.courseExecution.getCourseExecutionAggregateId(), this.courseExecution.getCourseExecutionVersion(), UpdateExecutionStudentNameEvent.class.getSimpleName(), this));
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
        Answer toCommitAnswer = new Answer(this);
        Answer committedAnswer = (Answer) committedVersion;
        mergeCourseExecution(toCommitAnswer, committedAnswer);
        mergeUser(toCommitAnswer, committedAnswer);
        mergeQuiz(toCommitAnswer, committedAnswer);
        mergeAnswerDate(toCommitVersionChangedFields, toCommitAnswer, committedAnswer);
        mergeQuestionAnswers((Answer)getPrev(), this, committedAnswer, toCommitAnswer);
        return toCommitAnswer;
    }

    private void mergeCourseExecution(Answer toCommitAnswer, Answer committedAnswer) {
        // The course execution version determines which user is more recent because the user is an execution student
        if(toCommitAnswer.getCourseExecution().getCourseExecutionVersion() >= committedAnswer.getCourseExecution().getCourseExecutionVersion()) {
            toCommitAnswer.getCourseExecution().setCourseExecutionVersion(toCommitAnswer.getCourseExecution().getCourseExecutionVersion());
        } else {
            toCommitAnswer.getCourseExecution().setCourseExecutionVersion(committedAnswer.getCourseExecution().getCourseExecutionVersion());
        }
    }

    private void mergeUser(Answer toCommitAnswer, Answer committedAnswer) {
        // The course execution version determines which user is more recent because the user is an execution student
        /*if(toCommitAnswer.getCourseExecution().getVersion() >= committedAnswer.getCourseExecution().getVersion()) {
            toCommitAnswer.getUser(getUser());
        } else {
            toCommitAnswer.getUser().(committedAnswer.getUser());
        }*/
    }

    private void mergeQuiz(Answer toCommitAnswer, Answer committedAnswer) {
        if(getQuiz().getQuizVersion() >= committedAnswer.getQuiz().getQuizVersion()) {
            toCommitAnswer.setQuiz(new AnswerQuiz(toCommitAnswer.getQuiz()));
        } else {
            toCommitAnswer.setQuiz(new AnswerQuiz(committedAnswer.getQuiz()));

        }
    }

    private void mergeAnswerDate(Set<String> toCommitVersionChangedFields, Answer toCommitAnswer, Answer committedAnswer) {
        if(toCommitVersionChangedFields.contains("answerDate")) {
            toCommitAnswer.setAnswerDate(getAnswerDate());
        } else {
            toCommitAnswer.setAnswerDate(committedAnswer.getAnswerDate());
        }
    }

    private static void mergeQuestionAnswers(Answer prev, Answer v1, Answer v2, Answer mergedAnswer) {
        /* Here we "calculate" the result of the incremental fields. This fields will always be the same regardless
         * of the base we choose. */

        Set<QuestionAnswer> prevQuestionAnswersPre = new HashSet<>(prev.getQuestionAnswers());
        Set<QuestionAnswer> v1QuestionAnswersPre = new HashSet<>(v1.getQuestionAnswers());
        Set<QuestionAnswer> v2QuestionAnswersPre = new HashSet<>(v2.getQuestionAnswers());

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

    public AnswerCourseExecution getCourseExecution() {
        return courseExecution;
    }

    public AnswerUser getUser() {
        return user;
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
            throw new TutorException(QUESTION_ALREADY_ANSWERED, questionAnswer.getQuestionAggregateId(), this.getQuiz().getQuizAggregateId());
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
