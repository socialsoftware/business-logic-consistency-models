package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Entity
public class QuizCourseExecution extends AggregateComponent {

    public QuizCourseExecution() {
        super();
    }

    public QuizCourseExecution(CourseExecutionDto courseExecutionDto) {
        super(courseExecutionDto.getAggregateId(), courseExecutionDto.getVersion());
    }

    public QuizCourseExecution(QuizCourseExecution other) {
        super(other.getAggregateId(), other.getVersion());
    }
}
