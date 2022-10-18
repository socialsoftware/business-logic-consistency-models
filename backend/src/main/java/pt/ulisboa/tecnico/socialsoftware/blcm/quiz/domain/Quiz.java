package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.*;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.*;
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
    private LocalDateTime availableDate; // changes

    @Column(name = "conclusion_date")
    private LocalDateTime conclusionDate; // changes

    @Column(name = "results_date")
    private LocalDateTime resultsDate; // changes

    @Column(nullable = false)
    private String title = "Title";

    @ElementCollection
    private Set<QuizQuestion> quizQuestions = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private QuizType quizType;

    /*
        COURSE_EXECUTION_FINAL
     */
    @Embedded
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
        super(other.getAggregateId(), QUIZ);
        setId(null);
        setQuizQuestions(other.getQuizQuestions());
        this.courseExecution = other.getCourseExecution();
        setTitle(other.getTitle());
        this.creationDate = other.getCreationDate();
        setAvailableDate(other.getAvailableDate());
        setConclusionDate(other.getConclusionDate());
        setResultsDate(other.getResultsDate());
        setQuizType(other.getQuizType());
        setQuizQuestions(new HashSet<>(other.getQuizQuestions()));
        setProcessedEvents(new HashMap<>(other.getProcessedEvents()));
        setEmittedEvents(new HashMap<>(other.getEmittedEvents()));
        setPrev(other);
    }

    //@Override
    /*public Aggregate merge(Aggregate other) {*/
        /*
            GENERATED_QUIZ
                INCREMENTAL FIELDS: NONE
            NON_GENERATED_QUIZ
                INCREMENTAL FIELDS: NONE
         */
        /*if(getQuizType().equals(QuizType.GENERATED)) {
            return mergeGenerated(other);
        } else {
            //mergeNormal(other);
            return this;
        }
    }*/

    /*private Aggregate mergeGenerated(Aggregate other) {
        if(!(other instanceof Quiz)) {
            throw new TutorException(ErrorMessage.QUIZ_MERGE_FAILURE, getAggregateId());
        }
        Quiz v2 = (Quiz)other;

        if(getState() == DELETED) {
            throw new TutorException(QUIZ_DELETED, getAggregateId());
        }

        if(v2.getState() == DELETED) {
            throw new TutorException(QUIZ_DELETED, v2.getAggregateId());
        }

        Set<String> v1ChangedFields = getChangedFields((Quiz)getPrev(), this);
        Set<String> v2ChangedFields = getChangedFields((Quiz)getPrev(), v2);

        if (checkNonIncrementalChanges(v1ChangedFields, v2ChangedFields) || checkNonIncrementalChanges(v2ChangedFields, v1ChangedFields)) {
            throw new TutorException(QUIZ_MERGE_FAILURE, getPrev().getAggregateId());
        }

        Quiz mergedQuiz = new Quiz(this);


        // TODO refer in thesis we assign the prev of the current aggregate because we want to preserve it case another merge round is executed
        // TODO once it's committed the prev is no longer relevant, because it will be other version will try to merge with this and that version contains its own prev
        mergedQuiz.setPrev((Quiz)getPrev());

        return mergedQuiz;
    }*/

    private Set<String> getChangedFields(Quiz prev, Quiz v) {
        Set<String> v1ChangedFields = new HashSet<>();
        /*if(!prev.getAvailableDate().equals(v.getAvailableDate())) {
            v1ChangedFields.add("availableDate");
        }

        if(!prev.getConclusionDate().equals(v.getConclusionDate())) {
            v1ChangedFields.add("conclusionDate");
        }

        if(!prev.getResultsDate().equals(v.getResultsDate())) {
            v1ChangedFields.add("resultsDate");
        }*/

        /*if(!prev.getQuizQuestions().equals(v.getQuizQuestions())) {
            v1ChangedFields.add("quizQuestions");
        }*/

        /*if(!prev.getTitle().equals(v.getTitle())) {
            v1ChangedFields.add("title");
        }*/

        return v1ChangedFields;
    }

    /*private static boolean checkNonIncrementalChanges(Set<String> v1ChangedFields, Set<String> v2ChangedFields) {
        if(v1ChangedFields.contains("availableDate")
                && (v2ChangedFields.contains("availableDate") ||
                v2ChangedFields.contains("conclusionDate") ||
                v2ChangedFields.contains("resultsDate"))) {

            return true;
        }

        if(v1ChangedFields.contains("conclusionDate")
                && (v2ChangedFields.contains("availableDate") ||
                v2ChangedFields.contains("conclusionDate") ||
                v2ChangedFields.contains("resultsDate"))) {

            return true;
        }

        if(v1ChangedFields.contains("resultsDate")
                && (v2ChangedFields.contains("availableDate") ||
                v2ChangedFields.contains("conclusionDate") ||
                v2ChangedFields.contains("resultsDate"))) {

            return true;
        }

        // TODO verify what to do with this
        // TODO verify equals in tournament aggregate components
        if(v1ChangedFields.contains("quizQuestions")
                && (v2ChangedFields.contains("quizQuestions"))) {

            return true;
        }


        if(v1ChangedFields.contains("title")
                && (v2ChangedFields.contains("title"))) {

            return true;
        }

        return false;
    }*/

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
    public Set<String> getEventSubscriptions() {
        return Set.of(REMOVE_COURSE_EXECUTION, UPDATE_QUESTION, REMOVE_QUESTION);
    }

    @Override
    public Set<String> getFieldsChangedByFunctionalities() {
        return Set.of("availableDate", "conclusionDate", "resultsDate", "quizQuestions");
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
        return null;
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
