package pt.ulisboa.tecnico.socialsoftware.ms.execution.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.event.subscribe.CourseExecutionSubscribesRemoveUser;
import pt.ulisboa.tecnico.socialsoftware.ms.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.ms.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.ms.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.utils.DateHandler;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.aggregate.domain.AggregateType.COURSE_EXECUTION;

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
    private String acronym;
    private String academicTerm;
    private LocalDateTime endDate;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "courseExecution")
    private CourseExecutionCourse courseExecutionCourse;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "courseExecution")
    private Set<CourseExecutionStudent> students = new HashSet<>();

    public CourseExecution() {

    }


    public CourseExecution(Integer aggregateId, CourseExecutionDto courseExecutionDto, CourseExecutionCourse courseExecutionCourse) {
        super(aggregateId, COURSE_EXECUTION);
        setAcronym(courseExecutionDto.getAcronym());
        setAcademicTerm(courseExecutionDto.getAcademicTerm());
        setEndDate(DateHandler.toLocalDateTime(courseExecutionDto.getEndDate()));
        setExecutionCourse(courseExecutionCourse);
    }


    public CourseExecution(CourseExecution other) {
        super(other);
        setAcronym(other.getAcronym());
        setAcademicTerm(other.getAcademicTerm());
        setEndDate(other.getEndDate());
        setExecutionCourse(new CourseExecutionCourse(other.getExecutionCourse()));
        setStudents(other.getStudents().stream().map(CourseExecutionStudent::new).collect(Collectors.toSet()));
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
        for (CourseExecutionStudent student : this.students) {
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
        Set<CourseExecutionStudent> prevStudentsPre = new HashSet<>(prev.getStudents());
        Set<CourseExecutionStudent> toCommitStudentsPre = new HashSet<>(toCommitQuiz.getStudents());
        Set<CourseExecutionStudent> committedStudentsPre = new HashSet<>(committedQuiz.getStudents());

        CourseExecutionStudent.syncStudentVersions(prevStudentsPre, toCommitStudentsPre, committedStudentsPre);

        Set<CourseExecutionStudent> prevStudents = new HashSet<>(prevStudentsPre);
        Set<CourseExecutionStudent> toCommitQuizStudents = new HashSet<>(toCommitStudentsPre);
        Set<CourseExecutionStudent> committedQuizStudents = new HashSet<>(committedStudentsPre);


        Set<CourseExecutionStudent> addedStudents =  SetUtils.union(
                SetUtils.difference(toCommitQuizStudents, prevStudents),
                SetUtils.difference(committedQuizStudents, prevStudents)
        );

        Set<CourseExecutionStudent> removedStudents = SetUtils.union(
                SetUtils.difference(prevStudents, toCommitQuizStudents),
                SetUtils.difference(prevStudents, committedQuizStudents)
        );

        Set<CourseExecutionStudent> mergedStudents = SetUtils.union(SetUtils.difference(prevStudents, removedStudents), addedStudents);
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

    public CourseExecutionCourse getExecutionCourse() {
        return courseExecutionCourse;
    }

    public void setExecutionCourse(CourseExecutionCourse course) {
        this.courseExecutionCourse = course;
        this.courseExecutionCourse.setCourseExecution(this);
    }

    public Set<CourseExecutionStudent> getStudents() {
        return students;
    }

    public void setStudents(Set<CourseExecutionStudent> students) {
        this.students = students;
        this.students.forEach(courseExecutionStudent -> courseExecutionStudent.setCourseExecution(this));
    }

    public void addStudent(CourseExecutionStudent courseExecutionStudent) {
        this.students.add(courseExecutionStudent);
        courseExecutionStudent.setCourseExecution(this);
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
        for (CourseExecutionStudent student : getStudents()) {
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
        if (this.courseExecutionCourse.getCourseVersion() == null) {
            this.courseExecutionCourse.setCourseVersion(version);
        }
        super.setVersion(version);
    }

    public boolean hasStudent(Integer userAggregateId) {
        for (CourseExecutionStudent student : this.students) {
            if (student.getUserAggregateId().equals(userAggregateId)) {
                return true;
            }
        }
        return false;
    }

    public CourseExecutionStudent findStudent(Integer userAggregateId) {
        for (CourseExecutionStudent student : this.students) {
            if (student.getUserAggregateId().equals(userAggregateId)) {
                return student;
            }
        }
        return null;
    }

    public void removeStudent(Integer userAggregateId) {
        CourseExecutionStudent studentToRemove = null;
        if (!hasStudent(userAggregateId)) {
            throw new TutorException(ErrorMessage.COURSE_EXECUTION_STUDENT_NOT_FOUND, userAggregateId, getAggregateId());
        }
        for (CourseExecutionStudent student : this.students) {
            if (student.getUserAggregateId().equals(userAggregateId)) {
                studentToRemove = student;
            }
        }
        this.students.remove(studentToRemove);
    }
}
