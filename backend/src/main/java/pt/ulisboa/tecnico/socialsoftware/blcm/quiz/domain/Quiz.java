package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.EventualConsistencyDependency;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.*;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Entity
@Table(name = "quizzes")
public class Quiz extends Aggregate {

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "available_date")
    private LocalDateTime availableDate; // changes

    @Column(name = "conclusion_date")
    private LocalDateTime conclusionDate; // changes

    @Column(name = "results_date")
    private LocalDateTime resultsDate; // changes

    @Column(nullable = false)
    private String title = "Title";

    @ElementCollection
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private QuizType quizType;

    @Embedded
    private QuizCourseExecution courseExecution;

    public Quiz() {

    }


    public Quiz(Integer aggregateId, QuizCourseExecution courseExecution, List<QuizQuestion> quizQuestions, QuizDto quizDto, QuizType quizType) {
        super(aggregateId, QUIZ);
        setCourseExecution(courseExecution);
        setQuizQuestions(quizQuestions);
        setTitle(quizDto.getTitle());
        setCreationDate(LocalDateTime.now());
        setAvailableDate(DateHandler.toLocalDateTime(quizDto.getAvailableDate()));
        setConclusionDate(DateHandler.toLocalDateTime(quizDto.getConclusionDate()));
        setResultsDate(DateHandler.toLocalDateTime(quizDto.getResultsDate()));
        setQuizType(quizType);
        setPrev(null);
    }

    public Quiz(Quiz other) {
        super(other.getAggregateId(), QUIZ);
        setId(null);
        setQuizQuestions(other.getQuizQuestions());
        setCourseExecution(other.getCourseExecution());
        setTitle(other.getTitle());
        setCreationDate(other.getCreationDate());
        setAvailableDate(other.getAvailableDate());
        setConclusionDate(other.getConclusionDate());
        setResultsDate(other.getResultsDate());
        setQuizType(other.getQuizType());
        setPrev(other);
        setQuizQuestions(new ArrayList<>(other.getQuizQuestions()));
    }

    @Override
    public Aggregate merge(Aggregate other) {
        /*
            GENERATED_QUIZ
                INCREMENTAL FIELDS: NONE
            NON_GENERATED_QUIZ
                INCREMENTAL FIELDS: NONE
         */
        if(getQuizType().equals(QuizType.GENERATED)) {
            return mergeGenerated(other);
        } else {
            //mergeNormal(other);
            return this;
        }
    }

    private Aggregate mergeGenerated(Aggregate other) {
        if(!(other instanceof Quiz)) {
            throw new TutorException(ErrorMessage.QUIZ_MERGE_FAILURE, getAggregateId());
        }
        Quiz v2 = (Quiz)other;

        if(getState().equals(DELETED)) {
            throw new TutorException(QUIZ_DELETED, getAggregateId());
        }

        if(v2.getState().equals(DELETED)) {
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
    }

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

        if(!prev.getTitle().equals(v.getTitle())) {
            v1ChangedFields.add("title");
        }

        return v1ChangedFields;
    }

    private static boolean checkNonIncrementalChanges(Set<String> v1ChangedFields, Set<String> v2ChangedFields) {
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
    }

    @Override
    public Map<Integer, EventualConsistencyDependency> getDependenciesMap() {
        Map<Integer, EventualConsistencyDependency> depMap = new HashMap<>();
        depMap.put(this.courseExecution.getAggregateId(), new EventualConsistencyDependency(this.courseExecution.getAggregateId(), COURSE_EXECUTION, this.courseExecution.getVersion()));
        quizQuestions.forEach(q -> {
            depMap.put(q.getAggregateId(), new EventualConsistencyDependency(q.getAggregateId(), QUESTION, q.getVersion()));
        });
        return depMap;
    }

    @Override
    public boolean verifyInvariants() {
        return true;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDateTime availableDate) {
        this.availableDate = availableDate;
    }

    public LocalDateTime getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(LocalDateTime conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public LocalDateTime getResultsDate() {
        return resultsDate;
    }

    public void setResultsDate(LocalDateTime resultsDate) {
        this.resultsDate = resultsDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<QuizQuestion> getQuizQuestions() {
        return quizQuestions;
    }


    public void setQuizQuestions(List<QuizQuestion> quizQuestions) {
        this.quizQuestions = quizQuestions;
    }

    public QuizCourseExecution getCourseExecution() {
        return courseExecution;
    }

    public void setCourseExecution(QuizCourseExecution courseExecution) {
        this.courseExecution = courseExecution;
    }

    public QuizType getQuizType() {
        return quizType;
    }

    public void setQuizType(QuizType quizType) {
        this.quizType = quizType;
    }

    public void update(QuizDto quizDto) {
        setTitle(quizDto.getTitle());
        setCreationDate(LocalDateTime.now());
        setAvailableDate(DateHandler.toLocalDateTime(quizDto.getAvailableDate()));
        setConclusionDate(DateHandler.toLocalDateTime(quizDto.getConclusionDate()));
        setResultsDate(DateHandler.toLocalDateTime(quizDto.getResultsDate()));
    }
}
