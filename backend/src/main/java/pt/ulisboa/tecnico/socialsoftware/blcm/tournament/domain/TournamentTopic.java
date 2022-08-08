package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TournamentTopic {
    @Column(name = "topic_aggregate_id")
    private Integer aggregateId;

    @Column(name = "topic_name")
    private String name;

    @Column(name = "topic_course_aggregate_id")
    private Integer courseAggregateId;

    public TournamentTopic() {

    }
    public TournamentTopic(Integer aggregateId, String name, Integer courseAggregateId) {
        this.aggregateId = aggregateId;
        this.name = name;
        this.courseAggregateId = courseAggregateId;
    }



    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer id) {
        this.aggregateId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCourseAggregateId() {
        return courseAggregateId;
    }

    public void setCourseAggregateId(Integer courseId) {
        this.courseAggregateId = courseId;
    }
}
