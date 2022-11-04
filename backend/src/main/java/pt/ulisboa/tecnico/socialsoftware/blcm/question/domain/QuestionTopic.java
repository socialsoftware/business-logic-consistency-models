package pt.ulisboa.tecnico.socialsoftware.blcm.question.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentTopic;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.util.Set;

@Entity
public class QuestionTopic extends AggregateComponent {
    @Column(name = "topic_aggregate_id")
    private Integer aggregateId;

    @Column(name = "topic_name")
    private String name;
    @Column(name = "topic_version")
    private Integer version;

    @Column(name = "topic_state")
    private Aggregate.AggregateState state;

    public QuestionTopic() {
        super();
    }

    public QuestionTopic (TopicDto topicDto) {
        super(topicDto.getAggregateId(), topicDto.getVersion());
        setName(topicDto.getName());
    }

    public QuestionTopic(QuestionTopic other) {
        super(other.getAggregateId(), other.getVersion());
        setName(other.getName());
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Aggregate.AggregateState getState() {
        return state;
    }

    public void setState(Aggregate.AggregateState state) {
        this.state = state;
    }

    public TopicDto buildDto() {
        TopicDto dto = new TopicDto();
        dto.setAggregateId(this.aggregateId);
        dto.setName(this.name);
        dto.setVersion(this.version);
        return dto;
    }

    public static void syncTopicVersions(Set<QuestionTopic> prevTopics, Set<QuestionTopic> v1Topics, Set<QuestionTopic> v2Topics) {
        for(QuestionTopic t1 : v1Topics) {
            for(QuestionTopic t2 : v2Topics) {
                if(t1.getAggregateId().equals(t2.getAggregateId())) {
                    if(t1.getVersion() > t2.getVersion()) {
                        t2.setVersion(t1.getVersion());
                        t2.setName(t1.getName());
                    }

                    if(t2.getVersion() > t1.getVersion()) {
                        t1.setVersion(t2.getVersion());
                        t1.setName(t2.getName());
                    }
                }
            }

            // no need to check again because the prev does not contain any newer version than v1 an v2
            for(QuestionTopic tp2 : prevTopics) {
                if(t1.getAggregateId().equals(tp2.getAggregateId())) {
                    if(t1.getVersion() > tp2.getVersion()) {
                        tp2.setVersion(t1.getVersion());
                        tp2.setName(t1.getName());
                    }

                    if(tp2.getVersion() > t1.getVersion()) {
                        t1.setVersion(tp2.getVersion());
                        t1.setName(tp2.getName());
                    }
                }
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getAggregateId();
        hash = 31 * hash + (getVersion() == null ? 0 : getVersion().hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof QuestionTopic)) {
            return false;
        }
        QuestionTopic tournamentTopic = (QuestionTopic) obj;

        return getAggregateId() != null && getAggregateId().equals(tournamentTopic.getAggregateId()) &&
                getVersion() != null && getVersion().equals(tournamentTopic.getVersion());
    }
}
