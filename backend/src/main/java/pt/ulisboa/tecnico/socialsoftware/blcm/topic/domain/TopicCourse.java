package pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.course.dto.CourseDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TopicCourse {
    @Column(name = "course_aggregate_id")
    private Integer aggregateId;

    @Column(name = "course_version")
    private Integer version;

    public TopicCourse() {}

    public TopicCourse(CourseDto courseDto) {
        setAggregateId(courseDto.getAggregateId());
        setVersion(courseDto.getVersion());
    }

    public TopicCourse(TopicCourse other) {
        setAggregateId(other.getAggregateId());
        setVersion(other.getVersion());
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer courseAggregateId) {
        this.aggregateId = courseAggregateId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
