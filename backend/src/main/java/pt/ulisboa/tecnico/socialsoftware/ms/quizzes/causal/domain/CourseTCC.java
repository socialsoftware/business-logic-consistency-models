package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.causal.domain;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.causal.CausalConsistency;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.course.domain.Course;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.modules.execution.dto.CourseExecutionDto;

import java.util.HashSet;
import java.util.Set;

@Entity
public class CourseTCC extends Course implements CausalConsistency {
    public CourseTCC() {
        super();
    }

    public CourseTCC(Integer aggregateId, CourseExecutionDto courseExecutionDto) {
        super(aggregateId, courseExecutionDto);
    }

    public CourseTCC(CourseTCC other) {
        super(other);
    }

    @Override
    public Set<String> getFieldsChangedByFunctionalities() {
        return new HashSet<>();
    }

    @Override
    public Set<String[]> getIntentions() {
        return new HashSet<>();
    }

    @Override
    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields) {
        return null;
    }
}
