package pt.ulisboa.tecnico.socialsoftware.blcm.question.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.dto.CourseDto;

@Embeddable
public class QuestionCourse {
    private Integer courseAggregateId;
    private String courseName;
    private Integer courseVersion;

    public QuestionCourse() {

    }
    public QuestionCourse(CourseDto causalCourseRemote) {
        setCourseAggregateId(causalCourseRemote.getAggregateId());
        setCourseName(causalCourseRemote.getName());
        setCourseVersion(causalCourseRemote.getVersion());
    }

    public QuestionCourse(QuestionCourse other) {
        setCourseAggregateId(other.getCourseAggregateId());
        setCourseName(other.getCourseName());
        setCourseVersion(other.getCourseVersion());
    }

    public Integer getCourseAggregateId() {
        return courseAggregateId;
    }

    public void setCourseAggregateId(Integer courseAggregateId) {
        this.courseAggregateId = courseAggregateId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getCourseVersion() {
        return courseVersion;
    }

    public void setCourseVersion(Integer courseVersion) {
        this.courseVersion = courseVersion;
    }

    public CourseDto buildDto() {
        CourseDto dto = new CourseDto();
        dto.setAggregateId(this.courseAggregateId);
        setCourseName(this.courseName);
        return dto;
    }
}
