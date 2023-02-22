package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

@Embeddable
public class TournamentCourseExecution {
    private Integer courseExecutionAggregateId;
    private Integer courseExecutionCourseId;
    private String courseExecutionAcronym;
    private String courseExecutionStatus;
    private Integer courseExecutionVersion;

    public TournamentCourseExecution() { }
    public TournamentCourseExecution(CourseExecutionDto courseExecutionDto) {
        setCourseExecutionAggregateId(courseExecutionDto.getAggregateId());
        setCourseExecutionVersion(courseExecutionDto.getVersion());
        setCourseExecutionCourseId(courseExecutionDto.getCourseAggregateId());
        setCourseExecutionAcronym(courseExecutionDto.getAcronym());
        setCourseExecutionStatus(courseExecutionDto.getStatus());
    }

    public TournamentCourseExecution(TournamentCourseExecution other) {
        setCourseExecutionAggregateId(other.getCourseExecutionAggregateId());
        setCourseExecutionVersion(other.getCourseExecutionVersion());
        setCourseExecutionCourseId(other.getCourseExecutionCourseId());
        setCourseExecutionAcronym(other.getCourseExecutionAcronym());
        setCourseExecutionStatus(other.getCourseExecutionStatus());
    }


    public Integer getCourseExecutionAggregateId() {
        return courseExecutionAggregateId;
    }

    public void setCourseExecutionAggregateId(Integer id) {
        this.courseExecutionAggregateId = id;
    }

    public Integer getCourseExecutionCourseId() {
        return courseExecutionCourseId;
    }

    public void setCourseExecutionCourseId(Integer courseExecutionCourseId) {
        this.courseExecutionCourseId = courseExecutionCourseId;
    }

    public String getCourseExecutionAcronym() {
        return courseExecutionAcronym;
    }

    public void setCourseExecutionAcronym(String courseExecutionAcronym) {
        this.courseExecutionAcronym = courseExecutionAcronym;
    }

    public String getCourseExecutionStatus() {
        return courseExecutionStatus;
    }

    public void setCourseExecutionStatus(String courseExecutionStatus) {
        this.courseExecutionStatus = courseExecutionStatus;
    }

    public Integer getCourseExecutionVersion() {
        return courseExecutionVersion;
    }

    public void setCourseExecutionVersion(Integer courseExecutionVersion) {
        this.courseExecutionVersion = courseExecutionVersion;
    }

    public CourseExecutionDto buildDto() {
        CourseExecutionDto courseExecutionDto = new CourseExecutionDto();
        courseExecutionDto.setAggregateId(getCourseExecutionAggregateId());
        courseExecutionDto.setVersion(getCourseExecutionVersion());
        courseExecutionDto.setAcronym(getCourseExecutionAcronym());
        courseExecutionDto.setStatus(getCourseExecutionStatus());

        return courseExecutionDto;
    }
}
