package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.AggregateType;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.Option;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.Dependency;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.AggregateType.COURSE_EXECUTION;
import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.AggregateType.QUESTION;

@Entity
@Table(name = "quizzes")
public class Quiz extends Aggregate {

    private Aggregate prev;
    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "available_date")
    private LocalDateTime availableDate;

    @Column(name = "conclusion_date")
    private LocalDateTime conclusionDate;

    @Column(name = "results_date")
    private LocalDateTime resultsDate;

    @Column(nullable = false)
    private String title = "Title";

    @ElementCollection
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    @Embedded
    private QuizCourseExecution courseExecution;

    public Quiz() {

    }


    public Quiz(Integer aggregateId, QuizCourseExecution courseExecution, List<QuizQuestion> quizQuestions, QuizDto quizDto) {
        super(aggregateId);
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
        super(other.getAggregateId());
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
        return false;
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
