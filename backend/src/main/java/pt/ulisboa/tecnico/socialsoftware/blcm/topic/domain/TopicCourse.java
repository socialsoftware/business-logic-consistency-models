package pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.domain.Course;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.dto.CourseDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Entity
public class TopicCourse extends AggregateComponent {

    public TopicCourse() {
        super();
    }

    public TopicCourse(CourseDto courseDto) {
        super(courseDto.getAggregateId(), courseDto.getVersion());
    }

    public TopicCourse(TopicCourse other) {
        super(other.getAggregateId(), other.getVersion());
    }

}
