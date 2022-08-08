package pt.ulisboa.tecnico.socialsoftware.blcm.topic;

public class TopicDto {
    private Integer id;

    private Integer courseId;

    private String name;


    public TopicDto() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
