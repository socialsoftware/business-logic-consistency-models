package pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.domain.Course;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.QuizQuestion;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateType.COURSE_EXECUTION;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.REMOVE_USER;

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
        if(getState() == ACTIVE) {
            for (ExecutionStudent student : this.students) {
                interInvariantUsersExist(eventSubscriptions, student);
            }
        }
        return eventSubscriptions;
    }

    private static void interInvariantUsersExist(Set<EventSubscription> eventSubscriptions, ExecutionStudent student) {
        eventSubscriptions.add(new EventSubscription(student.getAggregateId(), student.getVersion(), REMOVE_USER));
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
