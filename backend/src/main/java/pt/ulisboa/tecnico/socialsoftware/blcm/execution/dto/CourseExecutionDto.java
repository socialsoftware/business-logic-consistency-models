package pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto;

import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;

import java.io.Serializable;

public class CourseExecutionDto implements Serializable {
    private Integer aggregateId;

    private Integer courseId;

    private String name;

    private String type;

    private String acronym;

    private String academicTerm;

    private String endDate;

    private String status;

    private Integer version;

    public CourseExecutionDto() {

    }


    public CourseExecutionDto(CourseExecution courseExecution) {
        setAggregateId(courseExecution.getAggregateId());
        setCourseId(courseExecution.getCourse().getAggregateId());
        setName(courseExecution.getCourse().getName());
        setType(courseExecution.getCourse().getType().toString());
        setAcronym(courseExecution.getAcronym());
        setAcademicTerm(courseExecution.getAcademicTerm());
        setStatus(courseExecution.getState().toString());
        setVersion(courseExecution.getVersion());
        //setEndDate(courseExecution.getEndDate().toString());
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getAcademicTerm() {
        return academicTerm;
    }

    public void setAcademicTerm(String academicTerm) {
        this.academicTerm = academicTerm;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
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

    public void setVersion(Integer version) {
        this.version = version;
    }


}
