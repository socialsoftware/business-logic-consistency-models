package pt.ulisboa.tecnico.socialsoftware.blcm.question.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.course.dto.CourseDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class QuestionCourse {
    @Column(name = "course_aggregate_id")
    private Integer aggregateId;

    @Column(name = "course_name")
    private String name;

    @Column(name = "course_version")
    private Integer version;


    public QuestionCourse() {

    }
    public QuestionCourse(CourseDto causalCourseRemote) {
        setAggregateId(causalCourseRemote.getAggregateId());
        setName(causalCourseRemote.getName());
        setVersion(causalCourseRemote.getVersion());
    }

    public QuestionCourse(QuestionCourse other) {
        setAggregateId(other.getAggregateId());
        setName(other.getName());
        setVersion(other.getVersion());
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public CourseDto buildDto() {
        CourseDto dto = new CourseDto();
        dto.setAggregateId(this.aggregateId);
        setName(this.name);
        return dto;
    }
}
