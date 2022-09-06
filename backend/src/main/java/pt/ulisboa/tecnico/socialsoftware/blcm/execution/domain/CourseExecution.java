package pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.AggregateType;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.Dependency;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "course_executions")
public class CourseExecution extends Aggregate {

    @ManyToOne(fetch = FetchType.LAZY)
    private CourseExecution prev;

    // TODO add course type??

    @Column
    private String acronym;

    @Column
    private String academicTerm;

    // TODO course execution status contained in aggregate status?

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Embedded
    private ExecutionCourse course;

    public CourseExecution() {

    }


    public CourseExecution(Integer aggregateId, Integer version, String acronym, String academicTerm, LocalDateTime endDate, ExecutionCourse executionCourse) {
        super(aggregateId, version);
        setAcronym(acronym);
        setAcademicTerm(academicTerm);
        setEndDate(endDate);
        setCourse(executionCourse);

    }


    public CourseExecution(CourseExecution other) {
        super(other.getAggregateId());
        setAcronym(other.getAcronym());
        setAcademicTerm(other.getAcademicTerm());
        setEndDate(other.getEndDate());
        setCourse(other.getCourse());
        setPrev(other);
    }
    


    @Override
    public CourseExecution getPrev() {
        return prev;
    }

    @Override
    public Aggregate merge(Aggregate other) {
        return this;
    }

    @Override
    public Map<Integer, Dependency> getDependenciesMap() {
        Map<Integer, Dependency> depMap = new HashMap<>();
        depMap.put(this.course.getAggregateId(), new Dependency(this.course.getAggregateId(), AggregateType.COURSE, this.course.getVersion()));
        return depMap;
    }

    public void setPrev(CourseExecution prev) {
        this.prev = prev;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getAcademicTerm() {
        return academicTerm;
    }

    public void setAcademicTerm(String academicTerm) {
        this.academicTerm = academicTerm;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public ExecutionCourse getCourse() {
        return course;
    }

    public void setCourse(ExecutionCourse course) {
        this.course = course;
    }


    @Override
    public boolean verifyInvariants() {
        return true;
    }
}
