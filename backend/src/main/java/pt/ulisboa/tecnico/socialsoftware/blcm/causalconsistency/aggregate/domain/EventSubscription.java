package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain.Answer;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.AnonymizeExecutionStudentEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.UnerollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.UpdateExecutionStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipant;

import javax.persistence.*;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventType.*;

@Entity
public class EventSubscription {

    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private Integer senderAggregateId;

    @Column
    private Integer senderLastVersion;

    @Column
    private String eventType;

    @ManyToOne
    private Aggregate subscriberAggregate;

    public EventSubscription() {

    }

    public EventSubscription(Integer senderAggregateId, Integer senderLastVersion, String eventType, Aggregate subscriberAggregate) {
        setSenderAggregateId(senderAggregateId);
        // this is for complex functionalities where we dont know the id of an aggregate we are creating
        if(senderLastVersion == null) {
            setSenderLastVersion(0);
        } else {
            setSenderLastVersion(senderLastVersion);
        }
        setEventType(eventType);
        setSubscriberAggregate(subscriberAggregate);
    }

    public EventSubscription(EventSubscription other) {
        setSenderAggregateId(other.getSenderAggregateId());
        setSenderLastVersion(other.getSenderLastVersion());
        setEventType(other.getEventType());
    }


    public boolean subscribesEvent(Event event) {
        if (!(getSenderAggregateId().equals(event.getAggregateId()) && getEventType().equals(event.getType()) && getSenderLastVersion() < event.getAggregateVersion())) {
            return false;
        }
        switch (event.getType()) {
            case ANONYMIZE_EXECUTION_STUDENT:
                AnonymizeExecutionStudentEvent anonymizeExecutionStudentEvent = (AnonymizeExecutionStudentEvent) event;
                return checkSpecialCases(anonymizeExecutionStudentEvent.getUserAggregateId());
            case UNENROLL_STUDENT:
                UnerollStudentFromCourseExecutionEvent unerollStudentFromCourseExecutionEvent = (UnerollStudentFromCourseExecutionEvent) event;
                return checkSpecialCases(unerollStudentFromCourseExecutionEvent.getUserAggregateId());
            case UPDATE_EXECUTION_STUDENT_NAME:
                UpdateExecutionStudentNameEvent updateExecutionStudentNameEvent = (UpdateExecutionStudentNameEvent) event;
                return checkSpecialCases(updateExecutionStudentNameEvent.getUserAggregateId());
            default:
                return true;
        }
    }

    private boolean checkSpecialCases(Integer eventAdditionalAggregateId) {
        switch (this.subscriberAggregate.getAggregateType()) {
            case TOURNAMENT:
                return checkTournamentSpecialCase(eventAdditionalAggregateId);
            case ANSWER:
                return checkAnswerSpecialCase(eventAdditionalAggregateId);
            default:
                return true;
        }
    }

    private boolean checkTournamentSpecialCase(Integer eventAdditionalAggregateId) {
        Tournament tournament = (Tournament) this.subscriberAggregate;
        boolean specialCases;
        if(tournament.getCreator().getAggregateId().equals(eventAdditionalAggregateId)) {
            specialCases = true;
        } else {
            specialCases = false;
            for(TournamentParticipant tournamentParticipant : tournament.getParticipants()) {
                if(tournamentParticipant.getAggregateId().equals(eventAdditionalAggregateId)) {
                    specialCases = true;
                }
            }
        }
        return specialCases;
    }

    private boolean checkAnswerSpecialCase(Integer eventAdditionalAggregateId) {
        Answer answer = (Answer) this.subscriberAggregate;
        if(answer.getUser().getAggregateId().equals(eventAdditionalAggregateId)) {
            return true;
        }
        return false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Aggregate getSubscriberAggregate() {
        return subscriberAggregate;
    }

    public void setSubscriberAggregate(Aggregate aggregate) {
        this.subscriberAggregate = aggregate;
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