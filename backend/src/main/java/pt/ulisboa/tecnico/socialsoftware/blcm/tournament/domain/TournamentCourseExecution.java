package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

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

    public TournamentCourseExecution() { }
    public TournamentCourseExecution(Integer aggregateId, Integer courseId, String acronym, String status) {
        this.aggregateId = aggregateId;
        this.courseId = courseId;
        this.acronym = acronym;
        this.status = status;
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
}
