package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TournamentCourseExecution {
    @Column(name = "course_execution_aggregate_id")
    private Integer aggregateId;
    @Column(name = "course_execution_course_id")
    private Integer courseId;
    @Column(name = "course_execution_acronym")
    private String acronym;
    @Column(name = "course_execution_status")
    private String status;
    @Column(name = "course_execution_version")
    private Integer version;

    public TournamentCourseExecution() { }
    public TournamentCourseExecution(CourseExecutionDto courseExecutionDto) {
        setAggregateId(courseExecutionDto.getAggregateId());
        setVersion(courseExecutionDto.getVersion());
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


    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer id) {
        this.aggregateId = id;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer courseExecutionVersion) {
        this.version = courseExecutionVersion;
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
