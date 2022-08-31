package pt.ulisboa.tecnico.socialsoftware.blcm.question;

import pt.ulisboa.tecnico.socialsoftware.blcm.course.CourseDto;

import javax.persistence.Embeddable;

@Embeddable
public class QuestionCourse {
    private Integer aggregateId;

    private String name;


    public QuestionCourse() {

    }
    public QuestionCourse(CourseDto causalCourseRemote) {
        setAggregateId(causalCourseRemote.getAggregateId());
        setName(causalCourseRemote.getName());
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

    public CourseDto buildDto() {
        CourseDto dto = new CourseDto();
        dto.setAggregateId(this.aggregateId);
        setName(this.name);
        return dto;
    }
}
