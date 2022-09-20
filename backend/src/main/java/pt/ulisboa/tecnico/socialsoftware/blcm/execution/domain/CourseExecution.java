package pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.EventualConsistencyDependency;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.COURSE_EXECUTION;

@Entity
@Table(name = "course_executions")
public class CourseExecution extends Aggregate {
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


    public CourseExecution(Integer aggregateId, CourseExecutionDto courseExecutionDto, ExecutionCourse executionCourse) {
        super(aggregateId, COURSE_EXECUTION);
        setAcronym(courseExecutionDto.getAcronym());
        setAcademicTerm(courseExecutionDto.getAcademicTerm());
        setEndDate(LocalDateTime.parse(courseExecutionDto.getEndDate()));
        setCourse(executionCourse);

    }


    public CourseExecution(CourseExecution other) {
        super(other.getAggregateId(), COURSE_EXECUTION);
        setAcronym(other.getAcronym());
        setAcademicTerm(other.getAcademicTerm());
        setEndDate(other.getEndDate());
        setCourse(other.getCourse());
        setPrev(other);
    }

    @Override
    public Aggregate merge(Aggregate other) {
        return this;
    }

    @Override
    public Map<Integer, EventualConsistencyDependency> getDependenciesMap() {
        Map<Integer, EventualConsistencyDependency> depMap = new HashMap<>();
        depMap.put(this.course.getAggregateId(), new EventualConsistencyDependency(this.course.getAggregateId(), AggregateType.COURSE, this.course.getVersion()));
        return depMap;
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

    @Override
    public void setVersion(Integer version) {
        // if the course version is null, it means it that we're creating during this transaction
        if(this.course.getVersion() == null) {
            this.course.setVersion(version);
        }
        super.setVersion(version);
    }
}
