package pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.COURSE_EXECUTION;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventType.REMOVE_USER;

/*
    INTRA-INVARIANTS
        REMOVE_NO_STUDENTS
        REMOVE_COURSE_IS_VALID
        ALL_STUDENTS_ARE_ACTIVE
    INTER-INVARIANTS
        USER_EXISTS
        COURSE_EXISTS (does it count? course doesn't send events)
 */

@Entity
@Table(name = "course_executions")
public class CourseExecution extends Aggregate {
    // TODO add course type??

    @Column
    private String acronym;

    @Column
    private String academicTerm;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Embedded
    private ExecutionCourse course;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<ExecutionStudent> students;

    public CourseExecution() {

    }


    public CourseExecution(Integer aggregateId, CourseExecutionDto courseExecutionDto, ExecutionCourse executionCourse) {
        super(aggregateId, COURSE_EXECUTION);
        setAcronym(courseExecutionDto.getAcronym());
        setAcademicTerm(courseExecutionDto.getAcademicTerm());
        setEndDate(LocalDateTime.parse(courseExecutionDto.getEndDate()));
        setCourse(executionCourse);
        setStudents(new HashSet<>());
    }


    public CourseExecution(CourseExecution other) {
        super(other.getAggregateId(), COURSE_EXECUTION);
        setAcronym(other.getAcronym());
        setAcademicTerm(other.getAcademicTerm());
        setEndDate(other.getEndDate());
        setCourse(other.getCourse());
        setStudents(new HashSet<>(other.getStudents()));
        setProcessedEvents(new HashMap<>(other.getProcessedEvents()));
        setEmittedEvents(new HashMap<>(other.getEmittedEvents()));
        setPrev(other);
    }

    @Override
    public Set<String> getEventSubscriptions() {
        return Set.of(REMOVE_USER);
    }

    @Override
    public Set<String> getFieldsChangedByFunctionalities() {
        return Set.of("students");
    }

    @Override
    public Set<String[]> getIntentions() {
        return new HashSet<>();
    }

    @Override
    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields) {
        return null;
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

    public Set<ExecutionStudent> getStudents() {
        return students;
    }

    public void setStudents(Set<ExecutionStudent> students) {
        this.students = students;
    }

    public void addStudent(ExecutionStudent executionStudent) {
        this.students.add(executionStudent);
    }

    /*
        REMOVE_NO_STUDENTS
     */
    public boolean removedNoStudents() {
        if(getState() == AggregateState.DELETED) {
            return getStudents().size() == 0;
        }
        return true;
    }

    public boolean allStudentsAreActive() {
        for(ExecutionStudent student : getStudents()) {
            if(!student.isActive()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void verifyInvariants() {
        if(!(removedNoStudents() && allStudentsAreActive())) {
            throw new TutorException(ErrorMessage.INVARIANT_BREAK, getAggregateId());
        }
    }

    @Override
    public void remove() {
        if(getStudents().size() > 0) {
            super.remove();
        } else {
            throw new TutorException(ErrorMessage.CANNOT_DELETE_COURSE_EXECUTION, getAggregateId());
        }
    }


    @Override
    public void setVersion(Integer version) {
        // if the course version is null, it means it that we're creating during this transaction
        if(this.course.getVersion() == null) {
            this.course.setVersion(version);
        }
        super.setVersion(version);
    }

    public boolean hasStudent(Integer userAggregateId) {
        for(ExecutionStudent student : this.students) {
            if (student.getAggregateId().equals(userAggregateId)) {
                return true;
            }
        }
        return false;
    }

    public ExecutionStudent findStudent(Integer userAggregateId) {
        for(ExecutionStudent student : this.students) {
            if(student.getAggregateId().equals(userAggregateId)) {
                return student;
            }
        }
        return null;
    }

    public void removeStudent(Integer userAggregateId) {
        ExecutionStudent studentToRemove = null;
        if(!hasStudent(userAggregateId)) {
            throw new TutorException(ErrorMessage.COURSE_EXECUTION_STUDENT_NOT_FOUND, userAggregateId, getAggregateId());
        }
        for(ExecutionStudent student : this.students) {
            if(student.getAggregateId().equals(userAggregateId)) {
                studentToRemove = student;
            }
        }
        this.students.remove(studentToRemove);
    }
}
