package pt.ulisboa.tecnico.socialsoftware.blcm.question.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.dto.CourseDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Entity
public class QuestionCourse extends AggregateComponent {
    @Column(name = "course_name")
    private String name;


    public QuestionCourse() {
        super();
    }
    public QuestionCourse(CourseDto courseDto) {
        super(courseDto.getAggregateId(), courseDto.getVersion());
        setName(courseDto.getName());
    }

    public QuestionCourse(QuestionCourse other) {
        super(other.getAggregateId(), other.getVersion());
        setName(other.getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CourseDto buildDto() {
        CourseDto dto = new CourseDto();
        dto.setAggregateId(getAggregateId());
        setName(this.name);
        return dto;
    }
}
