package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.tcc;

import jakarta.persistence.Entity;
import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.CausalConsistency;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.domain.CourseExecutionCourse;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.domain.CourseExecutionStudent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.dto.CourseExecutionDto;

import java.util.HashSet;
import java.util.Set;

@Entity
public class CourseExecutionTCC extends CourseExecution implements CausalConsistency {
    public CourseExecutionTCC() {
        super();
    }

    public CourseExecutionTCC(Integer aggregateId, CourseExecutionDto courseExecutionDto, CourseExecutionCourse courseExecutionCourse) {
        super(aggregateId, courseExecutionDto, courseExecutionCourse);
    }

    public CourseExecutionTCC(CourseExecutionTCC other) {
        super(other);
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
        CourseExecutionTCC mergedCourseExecution = new CourseExecutionTCC(this);
        CourseExecutionTCC committedCourseExecution = (CourseExecutionTCC) committedVersion;
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
}
