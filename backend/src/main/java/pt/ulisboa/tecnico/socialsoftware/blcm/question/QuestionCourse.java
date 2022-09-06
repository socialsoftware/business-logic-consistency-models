package pt.ulisboa.tecnico.socialsoftware.blcm.question;

import pt.ulisboa.tecnico.socialsoftware.blcm.course.CourseDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class QuestionCourse {
    @Column(name = "course_aggregate_id")
    private Integer courseAggregateId;

    private String name;

    private Integer version;


    public QuestionCourse() {

    }
    public QuestionCourse(CourseDto causalCourseRemote) {
        setCourseAggregateId(causalCourseRemote.getAggregateId());
        setName(causalCourseRemote.getName());
        setVersion(causalCourseRemote.getVersion());
    }

    public Integer getCourseAggregateId() {
        return courseAggregateId;
    }

    public void setCourseAggregateId(Integer aggregateId) {
        this.courseAggregateId = aggregateId;
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
        dto.setAggregateId(this.courseAggregateId);
        setName(this.name);
        return dto;
    }
}
