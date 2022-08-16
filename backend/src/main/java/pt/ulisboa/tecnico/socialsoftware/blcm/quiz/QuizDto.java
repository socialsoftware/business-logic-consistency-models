package pt.ulisboa.tecnico.socialsoftware.blcm.quiz;

public class QuizDto {
    private Integer aggregateId;

    private Integer version;

    public QuizDto(Quiz quiz) {

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
}
