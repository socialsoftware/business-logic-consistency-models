package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.AnonymizeExecutionStudentEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.UnerollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.UpdateExecutionStudentNameEvent;

import javax.persistence.*;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.*;

@Embeddable
public class EventSubscription {
    @Column
    private Integer senderAggregateId;

    @Column
    private Integer senderLastVersion;

    @Column
    private String eventType;

    @Column
    private Integer extraEventInfo;

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

    public EventSubscription(Integer senderAggregateId, Integer senderLastVersion, String eventType, Integer extraEventInfo) {
        this(senderAggregateId, senderLastVersion, eventType);
        setExtraEventInfo(extraEventInfo);
    }

    public EventSubscription(EventSubscription other) {
        setSenderAggregateId(other.getSenderAggregateId());
        setSenderLastVersion(other.getSenderLastVersion());
        setEventType(other.getEventType());
    }


    public boolean conformsToEvent(Event event) {
        boolean specialCases;
        switch (event.getType()) {
            case ANONYMIZE_EXECUTION_STUDENT:
                AnonymizeExecutionStudentEvent anonymizeExecutionStudentEvent = (AnonymizeExecutionStudentEvent) event;
                specialCases = this.extraEventInfo.equals(anonymizeExecutionStudentEvent.getUserAggregateId());
                break;
            case UNENROLL_STUDENT:
                UnerollStudentFromCourseExecutionEvent unerollStudentFromCourseExecutionEvent = (UnerollStudentFromCourseExecutionEvent) event;
                specialCases = this.extraEventInfo.equals(unerollStudentFromCourseExecutionEvent.getUserAggregateId());
                break;
            case UPDATE_EXECUTION_STUDENT_NAME:
                UpdateExecutionStudentNameEvent updateExecutionStudentNameEvent = (UpdateExecutionStudentNameEvent) event;
                specialCases = this.extraEventInfo.equals(updateExecutionStudentNameEvent.getUserAggregateId());
                break;
            default:
                specialCases = true;
                break;
        }
        return specialCases && getSenderAggregateId().equals(event.getAggregateId()) && getSenderLastVersion().equals(event.getAggregateVersion());
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

    public Integer getExtraEventInfo() {
        return extraEventInfo;
    }

    public void setExtraEventInfo(Integer extraEventInfo) {
        this.extraEventInfo = extraEventInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EventSubscription)) {
            return false;
        }
        EventSubscription other = (EventSubscription) obj;
        return getSenderAggregateId() != null && getSenderAggregateId().equals(other.getSenderAggregateId()) &&
                getSenderLastVersion() != null && getSenderLastVersion().equals(other.getSenderLastVersion()) &&
                getEventType() != null && getEventType().equals(other.getEventType()) &&
                getExtraEventInfo() != null && getExtraEventInfo().equals(((EventSubscription) obj).getExtraEventInfo());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getSenderAggregateId();
        hash = 31 * hash + (getSenderLastVersion() == null ? 0 : getSenderLastVersion().hashCode());
        hash = 31 * hash + ((getExtraEventInfo()) == null ? 0 : getExtraEventInfo().hashCode());
        hash = 31 * hash + getEventType().hashCode();
        return hash;
    }
}