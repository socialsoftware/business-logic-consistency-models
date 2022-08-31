package pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.course.CourseDto;

import javax.persistence.Embeddable;

@Embeddable
public class TopicCourse {
    private Integer courseAggregateId;

    public TopicCourse() {}

    public TopicCourse(CourseDto courseDto) {
        setAggregateId(courseDto.getAggregateId());
    }

    public Integer getAggregateId() {
        return courseAggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.courseAggregateId = aggregateId;
    }
}
