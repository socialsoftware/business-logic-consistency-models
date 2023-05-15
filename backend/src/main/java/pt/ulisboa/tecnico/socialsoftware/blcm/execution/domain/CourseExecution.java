package pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.event.subscribe.CourseExecutionSubscribesRemoveUser;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.COURSE_EXECUTION;

/*
    INTRA-INVARIANTS
        REMOVE_NO_STUDENTS
        REMOVE_COURSE_IS_VALID
        ALL_STUDENTS_ARE_ACTIVE
        CANNOT_REMOVE_IF_STUDENTS
    INTER-INVARIANTS
        USER_EXISTS
        COURSE_EXISTS (does it count? course doesn't send events)
 */

@Entity
public class CourseExecution extends Aggregate {
    @Column
    private String acronym;
    @Column
    private String academicTerm;
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
        setEndDate(DateHandler.toLocalDateTime(courseExecutionDto.getEndDate()));
        setCourse(executionCourse);
        setStudents(new HashSet<>());
    }


    public CourseExecution(CourseExecution other) {
        super(other);
        setAcronym(other.getAcronym());
        setAcademicTerm(other.getAcademicTerm());
        setEndDate(other.getEndDate());
        setCourse(other.getCourse());
        setStudents(new HashSet<>(other.getStudents().stream().map(ExecutionStudent::new).collect(Collectors.toSet())));

    }

    @Override
    public Set<EventSubscription> getEventSubscriptions() {
        Set<EventSubscription> eventSubscriptions = new HashSet<>();
        if (getState() == ACTIVE) {
            interInvariantUsersExist(eventSubscriptions);
        }
        return eventSubscriptions;
    }

    private void interInvariantUsersExist(Set<EventSubscription> eventSubscriptions) {
        for (ExecutionStudent student : this.students) {
            eventSubscriptions.add(new CourseExecutionSubscribesRemoveUser(student));
        }
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
        CourseExecution mergedCourseExecution = new CourseExecution(this);
        CourseExecution committedCourseExecution = (CourseExecution) committedVersion;
        mergeQuizQuestions((CourseExecution) getPrev(), this, committedCourseExecution, mergedCourseExecution);
        return mergedCourseExecution;
    }

    private void mergeQuizQuestions(CourseExecution prev, CourseExecution toCommitQuiz, CourseExecution committedQuiz, CourseExecution mergedCourseExecution) {
        Set<ExecutionStudent> prevStudentsPre = new HashSet<>(prev.getStudents());
        Set<ExecutionStudent> toCommitStudentsPre = new HashSet<>(toCommitQuiz.getStudents());
        Set<ExecutionStudent> committedStudentsPre = new HashSet<>(committedQuiz.getStudents());

        ExecutionStudent.syncStudentVersions(prevStudentsPre, toCommitStudentsPre, committedStudentsPre);

        Set<ExecutionStudent> prevStudents = new HashSet<>(prevStudentsPre);
        Set<ExecutionStudent> toCommitQuizStudents = new HashSet<>(toCommitStudentsPre);
        Set<ExecutionStudent> committedQuizStudents = new HashSet<>(committedStudentsPre);


        Set<ExecutionStudent> addedStudents =  SetUtils.union(
                SetUtils.difference(toCommitQuizStudents, prevStudents),
                SetUtils.difference(committedQuizStudents, prevStudents)
        );

        Set<ExecutionStudent> removedStudents = SetUtils.union(
                SetUtils.difference(prevStudents, toCommitQuizStudents),
                SetUtils.difference(prevStudents, committedQuizStudents)
        );

        Set<ExecutionStudent> mergedStudents = SetUtils.union(SetUtils.difference(prevStudents, removedStudents), addedStudents);
        mergedCourseExecution.setStudents(mergedStudents);
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
        if (getState() == AggregateState.DELETED) {
            return getStudents().size() == 0;
        }
        return true;
    }

    public boolean allStudentsAreActive() {
        for (ExecutionStudent student : getStudents()) {
            if(!student.isActive()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void verifyInvariants() {
        if (!(removedNoStudents() && allStudentsAreActive())) {
            throw new TutorException(ErrorMessage.INVARIANT_BREAK, getAggregateId());
        }
    }

    @Override
    public void remove() {
        /*
            CANNOT_REMOVE_IF_STUDENTS
         */
        if (getStudents().size() > 0) {
            super.remove();
        } else {
            throw new TutorException(ErrorMessage.CANNOT_DELETE_COURSE_EXECUTION, getAggregateId());
        }
    }


    @Override
    public void setVersion(Integer version) {
        // if the course version is null, it means it that we're creating during this transaction
        if (this.course.getCourseVersion() == null) {
            this.course.setCourseVersion(version);
        }
        super.setVersion(version);
    }

    public boolean hasStudent(Integer userAggregateId) {
        for (ExecutionStudent student : this.students) {
            if (student.getUserAggregateId().equals(userAggregateId)) {
                return true;
            }
        }
        return false;
    }

    public ExecutionStudent findStudent(Integer userAggregateId) {
        for (ExecutionStudent student : this.students) {
            if (student.getUserAggregateId().equals(userAggregateId)) {
                return student;
            }
        }
        return null;
    }

    public void removeStudent(Integer userAggregateId) {
        ExecutionStudent studentToRemove = null;
        if (!hasStudent(userAggregateId)) {
            throw new TutorException(ErrorMessage.COURSE_EXECUTION_STUDENT_NOT_FOUND, userAggregateId, getAggregateId());
        }
        for (ExecutionStudent student : this.students) {
            if (student.getUserAggregateId().equals(userAggregateId)) {
                studentToRemove = student;
            }
        }
        this.students.remove(studentToRemove);
    }
}
