package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.*;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.*;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

/*
    INTRA-INVARIANTS
        DATE_ORDERING
        QUESTIONS_FINAL_AFTER_AVAILABLE_DATE
        COURSE_EXECUTION_FINAL
        CREATION_DATE_FINAL
        AVAILABLE_DATE_FINAL_AFTER_AVAILABLE_DATE
        CONCLUSION_DATE_FINAL_AFTER_AVAILABLE_DATE
        RESULTS_DATE_FINAL_AFTER_AVAILABLE_DATE
    INTER-INVARIANTS
        QUESTION_EXISTS
        COURSE_EXECUTION_EXISTS
        QUESTION_COURSE_EXECUTION_SAME_AS_COURSE_EXECUTION
 */
@Entity
@Table(name = "quizzes")
public class Quiz extends Aggregate {

    /*
        CREATION_DATE_FINAL
     */
    @Column(name = "creation_date")
    private final LocalDateTime creationDate;

    @Column(name = "available_date")
    private LocalDateTime availableDate;

    @Column(name = "conclusion_date")
    private LocalDateTime conclusionDate;

    @Column(name = "results_date")
    private LocalDateTime resultsDate;

    @Column(nullable = false)
    private String title = "Title";

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<QuizQuestion> quizQuestions = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private QuizType quizType;

    /*
        COURSE_EXECUTION_FINAL
     */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private final QuizCourseExecution courseExecution;

    public Quiz() {
        this.courseExecution = null;
        this.creationDate = null;
    }


    public Quiz(Integer aggregateId, QuizCourseExecution courseExecution, Set<QuizQuestion> quizQuestions, QuizDto quizDto, QuizType quizType) {
        super(aggregateId, QUIZ);
        this.courseExecution = courseExecution;
        setQuizQuestions(quizQuestions);
        setTitle(quizDto.getTitle());
        this.creationDate = LocalDateTime.now();
        setAvailableDate(LocalDateTime.parse(quizDto.getAvailableDate()));
        setConclusionDate(LocalDateTime.parse(quizDto.getConclusionDate()));
        setResultsDate(LocalDateTime.parse(quizDto.getResultsDate()));
        setQuizType(quizType);
        setPrev(null);
    }

    public Quiz(Quiz other) {
        super(other);
        setQuizQuestions(other.getQuizQuestions());
        this.courseExecution = new QuizCourseExecution(other.getCourseExecution());
        setTitle(other.getTitle());
        this.creationDate = other.getCreationDate();
        setAvailableDate(other.getAvailableDate());
        setConclusionDate(other.getConclusionDate());
        setResultsDate(other.getResultsDate());
        setQuizType(other.getQuizType());
        setQuizQuestions(other.getQuizQuestions().stream().map(QuizQuestion::new).collect(Collectors.toSet()));
    }



    public boolean invariantDateOrdering() {
        return getCreationDate().isBefore(getConclusionDate()) &&
                getAvailableDate().isBefore(getConclusionDate()) &&
                (getConclusionDate().isEqual(getResultsDate()) || getConclusionDate().isBefore(getResultsDate()));

    }

    @Override
    public void verifyInvariants() {
        if(!(invariantDateOrdering())) {
            throw new TutorException(INVARIANT_BREAK, getAggregateId());
        }
    }

    @Override
    public Set<EventSubscription> getEventSubscriptions() {
        Set<EventSubscription> eventSubscriptions = new HashSet<>();
        if(getState() == ACTIVE) {
            interInvariantCourseExecutionExists(eventSubscriptions);
            interInvariantQuestionsExist(eventSubscriptions);
        }
        return eventSubscriptions;
    }

    private void interInvariantCourseExecutionExists(Set<EventSubscription> eventSubscriptions) {
        eventSubscriptions.add(new EventSubscription(this.courseExecution.getAggregateId(), this.courseExecution.getVersion(), REMOVE_COURSE_EXECUTION, this));
    }

    private void interInvariantQuestionsExist(Set<EventSubscription> eventSubscriptions) {
        for (QuizQuestion quizQuestion : this.quizQuestions) {
            eventSubscriptions.add(new EventSubscription(quizQuestion.getAggregateId(), quizQuestion.getVersion(), UPDATE_QUESTION, this));
            eventSubscriptions.add(new EventSubscription(quizQuestion.getAggregateId(), quizQuestion.getVersion(), REMOVE_QUESTION, this));
        }
    }


    @Override
    public Set<String> getFieldsChangedByFunctionalities() {
        // we dont add the courseExecution because it can only change through events and the only events that comes from it is the delete which deletes the quiz
        return Set.of("availableDate", "conclusionDate", "resultsDate", "title" ,"quizQuestions");
    }

    @Override
    public Set<String[]> getIntentions() {
        return Set.of(
                new String[]{"availableDate", "conclusionDate"},
                new String[]{"availableDate", "resultsDate"},
                new String[]{"conclusionDate", "resultsDate"});
    }

    @Override
    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields) {
        Quiz committedQuiz = (Quiz) committedVersion;
        Quiz mergedQuiz = new Quiz(this);

        mergeAvailableDate(toCommitVersionChangedFields, committedQuiz, mergedQuiz);
        mergeConclusionDate(toCommitVersionChangedFields, committedQuiz, mergedQuiz);
        mergeResultsDate(toCommitVersionChangedFields, committedQuiz, mergedQuiz);
        mergeTitle(toCommitVersionChangedFields, committedQuiz, mergedQuiz);
        mergeQuizQuestions(toCommitVersionChangedFields, committedQuiz, mergedQuiz);
        return mergedQuiz;
    }

    private void mergeAvailableDate(Set<String> toCommitVersionChangedFields, Quiz committedQuiz, Quiz mergedQuiz) {
        if(toCommitVersionChangedFields.contains("availableDate")) {
            mergedQuiz.setAvailableDate(getAvailableDate());
        } else {
            mergedQuiz.setAvailableDate(committedQuiz.getAvailableDate());
        }
    }

    private void mergeConclusionDate(Set<String> toCommitVersionChangedFields, Quiz committedQuiz, Quiz mergedQuiz) {
        if(toCommitVersionChangedFields.contains("conclusionDate")) {
            mergedQuiz.setConclusionDate(getConclusionDate());
        } else {
            mergedQuiz.setConclusionDate(committedQuiz.getConclusionDate());
        }
    }

    private void mergeResultsDate(Set<String> toCommitVersionChangedFields, Quiz committedQuiz, Quiz mergedQuiz) {
        if(toCommitVersionChangedFields.contains("resultsDate")) {
            mergedQuiz.setResultsDate(getResultsDate());
        } else {
            mergedQuiz.setResultsDate(committedQuiz.getResultsDate());
        }
    }

    private void mergeTitle(Set<String> toCommitVersionChangedFields, Quiz committedQuiz, Quiz mergedQuiz) {
        if(toCommitVersionChangedFields.contains("title")) {
            mergedQuiz.setTitle(getTitle());
        } else {
            mergedQuiz.setTitle(committedQuiz.getTitle());
        }
    }
    
    private void mergeQuizQuestions(Set<String> toCommitVersionChangedFields, Quiz committedQuiz, Quiz mergedQuiz) {
        if(toCommitVersionChangedFields.contains("quizQuestions")) {
            mergedQuiz.setQuizQuestions(getQuizQuestions().stream().map(QuizQuestion::new).collect(Collectors.toSet()));
        } else {
            mergedQuiz.setQuizQuestions(committedQuiz.getQuizQuestions().stream().map(QuizQuestion::new).collect(Collectors.toSet()));
        }
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDateTime availableDate) {
        /*
            AVAILABLE_DATE_FINAL_AFTER_AVAILABLE_DATE
         */
        Quiz prev = (Quiz) getPrev();
        if(prev != null && (LocalDateTime.now()).isAfter(prev.getAvailableDate())) {
            throw new TutorException(CANNOT_UPDATE_QUIZ, getAggregateId());
        }
        this.availableDate = availableDate;
    }

    public LocalDateTime getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(LocalDateTime conclusionDate) {
        /*
            CONCLUSION_DATE_FINAL_AFTER_AVAILABLE_DATE
         */
        Quiz prev = (Quiz) getPrev();
        if(prev != null && (LocalDateTime.now()).isAfter(prev.getAvailableDate())) {
            throw new TutorException(CANNOT_UPDATE_QUIZ, getAggregateId());
        }
        this.conclusionDate = conclusionDate;
    }

    public LocalDateTime getResultsDate() {
        return resultsDate;
    }

    public void setResultsDate(LocalDateTime resultsDate) {
        /*
            RESULTS_DATE_FINAL_AFTER_AVAILABLE_DATE
         */
        Quiz prev = (Quiz) getPrev();
        if(prev != null && (LocalDateTime.now()).isAfter(prev.getAvailableDate())) {
            throw new TutorException(CANNOT_UPDATE_QUIZ, getAggregateId());
        }
        this.resultsDate = resultsDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<QuizQuestion> getQuizQuestions() {
        return quizQuestions;
    }


    public void setQuizQuestions(Set<QuizQuestion> quizQuestions) {
        /*
            QUESTIONS_FINAL_AFTER_AVAILABLE_DATE
         */
        Quiz prev = (Quiz) getPrev();
        if(prev != null && LocalDateTime.now().isAfter(prev.getAvailableDate())) {
            throw new TutorException(CANNOT_UPDATE_QUIZ, getAggregateId());
        }
        this.quizQuestions = quizQuestions;
    }

    public QuizCourseExecution getCourseExecution() {
        return courseExecution;
    }

    public QuizType getQuizType() {
        return quizType;
    }

    public void setQuizType(QuizType quizType) {
        this.quizType = quizType;
    }

    public void update(QuizDto quizDto) {
        setTitle(quizDto.getTitle());
        setAvailableDate(LocalDateTime.parse(quizDto.getAvailableDate()));
        setConclusionDate(LocalDateTime.parse(quizDto.getConclusionDate()));
        setResultsDate(LocalDateTime.parse(quizDto.getResultsDate()));
    }

    public QuizQuestion findQuestion(Integer questionAggregateId) {
        for(QuizQuestion qq : quizQuestions) {
            if(qq.getAggregateId().equals(questionAggregateId)) {
                return qq;
            }
        }
        return null;
    }
}
