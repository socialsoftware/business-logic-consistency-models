package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.Dependency;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.*;

@Entity
@Table(name = "quizzes")
public class Quiz extends Aggregate {

    @ManyToOne(fetch = FetchType.LAZY)
    private Aggregate prev;
    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @Column(name = "creation_date")
    private LocalDateTime creationDate; // changes

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

    @Embedded
    private QuizCourseExecution courseExecution;

    public Quiz() {

    }


    public Quiz(Integer aggregateId, QuizCourseExecution courseExecution, List<QuizQuestion> quizQuestions, QuizDto quizDto) {
        super(aggregateId, QUIZ);
        setCourseExecution(courseExecution);
        setQuizQuestions(quizQuestions);
        setTitle(quizDto.getTitle());
        setCreationDate(LocalDateTime.now());
        setAvailableDate(DateHandler.toLocalDateTime(quizDto.getAvailableDate()));
        setConclusionDate(DateHandler.toLocalDateTime(quizDto.getConclusionDate()));
        setResultsDate(DateHandler.toLocalDateTime(quizDto.getResultsDate()));
        setPrev(null);
    }

    public Quiz(Quiz other) {
        super(other.getAggregateId(), QUIZ);
        setId(null);
        setQuizQuestions(quizQuestions);
        setTitle(other.getTitle());
        setCreationDate(other.getCreationDate());
        setAvailableDate(other.getAvailableDate());
        setConclusionDate(other.getConclusionDate());
        setResultsDate(other.getResultsDate());
        setPrev(other);
        setQuizQuestions(new ArrayList<>(other.getQuizQuestions()));
    }

    @Override
    public Aggregate merge(Aggregate other) {
        return this;
    }

    @Override
    public Map<Integer, Dependency> getDependenciesMap() {
        Map<Integer, Dependency> depMap = new HashMap<>();
        depMap.put(this.courseExecution.getAggregateId(), new Dependency(this.courseExecution.getAggregateId(), COURSE_EXECUTION, this.courseExecution.getVersion()));
        quizQuestions.forEach(q -> {
            depMap.put(q.getAggregateId(), new Dependency(q.getAggregateId(), QUESTION, q.getVersion()));
        });
        return depMap;
    }

    @Override
    public Aggregate getPrev() {
        return this.prev;
    }

    public void setPrev(Quiz quiz) {
        this.prev = quiz;
    }

    @Override
    public boolean verifyInvariants() {
        return true;
    }


    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
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

    public void update(QuizDto quizDto) {
        setTitle(quizDto.getTitle());
        setCreationDate(LocalDateTime.now());
        setAvailableDate(DateHandler.toLocalDateTime(quizDto.getAvailableDate()));
        setConclusionDate(DateHandler.toLocalDateTime(quizDto.getConclusionDate()));
        setResultsDate(DateHandler.toLocalDateTime(quizDto.getResultsDate()));
    }
}
