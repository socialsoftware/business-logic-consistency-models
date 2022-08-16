package pt.ulisboa.tecnico.socialsoftware.blcm.topic;

public class TopicDto {
    private Integer aggregateId;

    private Integer version;
    private Integer courseId;

    private String name;


    public TopicDto() {

    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getAcronym() {
        return name;
    }

    public void setAcronym(String acronym) {
        this.name = acronym;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
