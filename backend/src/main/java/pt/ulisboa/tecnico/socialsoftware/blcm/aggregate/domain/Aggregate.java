package pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain;

import java.time.LocalDateTime;

public interface Aggregate {

    enum AggregateState {
        ACTIVE,
        INACTIVE,
        DELETED
    }
    Integer getVersion();
    boolean verifyInvariants();
    LocalDateTime getCreationTs();
    void setCreationTs(LocalDateTime time);
    AggregateState getState();
    void setState(AggregateState state);
    Integer getAggregateId();
    Integer getId();
}
