package pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.dto.CourseDto;

@Embeddable
public class TopicCourse {
    private Integer courseAggregateId;
    private Integer courseVersion;

    public TopicCourse() {}

    public TopicCourse(CourseDto courseDto) {
        setCourseAggregateId(courseDto.getAggregateId());
        setCourseVersion(courseDto.getVersion());
    }

    public TopicCourse(TopicCourse other) {
        setCourseAggregateId(other.getCourseAggregateId());
        setCourseVersion(other.getCourseVersion());
    }

    public Integer getCourseAggregateId() {
        return courseAggregateId;
    }

    public void setCourseAggregateId(Integer courseAggregateId) {
        this.courseAggregateId = courseAggregateId;
    }

    public Integer getCourseVersion() {
        return courseVersion;
    }

    public void setCourseVersion(Integer courseVersion) {
        this.courseVersion = courseVersion;
    }
}
