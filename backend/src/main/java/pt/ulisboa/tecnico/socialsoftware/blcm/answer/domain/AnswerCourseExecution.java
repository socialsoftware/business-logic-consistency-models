package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Entity
public class AnswerCourseExecution extends AggregateComponent {


    public AnswerCourseExecution() {
        super();
    }

    public AnswerCourseExecution(Integer courseExecutionAggregateId, Integer courseExecutionVersion) {
        super(courseExecutionAggregateId, courseExecutionVersion);
    }

    public AnswerCourseExecution(AnswerCourseExecution courseExecution) {
        super(courseExecution.getAggregateId(), courseExecution.getVersion());
    }
}
