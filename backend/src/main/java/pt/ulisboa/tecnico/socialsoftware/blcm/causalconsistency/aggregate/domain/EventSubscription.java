package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain.QuizQuestion;

import javax.persistence.*;
import java.util.Set;

@Embeddable
public class EventSubscription {
    @Column
    private Integer senderAggregateId;

    @Column
    private Integer senderLastVersion;

    @Column
    private String eventType;

    public EventSubscription() {

    }

    public EventSubscription(Integer senderAggregateId, Integer senderLastVersion, String eventType) {
        setSenderAggregateId(senderAggregateId);
        // this is for complex functionalities where we dont know the id of an aggregate we are creating
        if(senderLastVersion == null) {
            setSenderLastVersion(0);
        } else {
            setSenderLastVersion(senderLastVersion);
        }
        setEventType(eventType);
    }

    public EventSubscription(EventSubscription other) {
        setSenderAggregateId(other.getSenderAggregateId());
        setSenderLastVersion(other.getSenderLastVersion());
        setEventType(other.getEventType());
    }


    public Integer getSenderAggregateId() {
        return senderAggregateId;
    }

    public void setSenderAggregateId(Integer senderAggregateId) {
        this.senderAggregateId = senderAggregateId;
    }

    public Integer getSenderLastVersion() {
        return senderLastVersion;
    }

    public void setSenderLastVersion(Integer senderLastVersion) {
        this.senderLastVersion = senderLastVersion;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EventSubscription)) {
            return false;
        }
        EventSubscription other = (EventSubscription) obj;
        return getSenderAggregateId() != null && getSenderAggregateId().equals(other.getSenderAggregateId()) &&
                getSenderLastVersion() != null && getSenderLastVersion().equals(other.getSenderLastVersion()) &&
                getEventType() != null && getEventType().equals(other.getEventType());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getSenderAggregateId();
        hash = 31 * hash + (getSenderLastVersion() == null ? 0 : getSenderLastVersion().hashCode());
        hash = 31 * hash + getEventType().hashCode();

        return hash;
    }
}