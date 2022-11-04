package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Entity
public class TournamentCourseExecution extends AggregateComponent {


    @Column(name = "course_execution_course_id")
    private Integer courseId;

    @Column(name = "course_execution_acronym")
    private String acronym;

    @Column(name = "course_execution_status")
    private String status;




    public TournamentCourseExecution() {
        super();
    }
    public TournamentCourseExecution(CourseExecutionDto courseExecutionDto) {
        super(courseExecutionDto.getAggregateId(), courseExecutionDto.getVersion());
        setCourseId(courseExecutionDto.getCourseAggregateId());
        setAcronym(courseExecutionDto.getAcronym());
        setStatus(courseExecutionDto.getStatus());
    }

    public TournamentCourseExecution(TournamentCourseExecution other) {
        setAggregateId(other.getAggregateId());
        setVersion(other.getVersion());
        setCourseId(other.getCourseId());
        setAcronym(other.getAcronym());
        setStatus(other.getStatus());
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public CourseExecutionDto buildDto() {
        CourseExecutionDto courseExecutionDto = new CourseExecutionDto();
        courseExecutionDto.setAggregateId(getAggregateId());
        courseExecutionDto.setVersion(getVersion());
        courseExecutionDto.setAcronym(getAcronym());
        courseExecutionDto.setStatus(getStatus());

        return courseExecutionDto;
    }
}
