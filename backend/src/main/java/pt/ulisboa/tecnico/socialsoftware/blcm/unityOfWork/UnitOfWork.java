package pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.DomainEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.version.service.VersionService;

import javax.persistence.PostLoad;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;


public class UnitOfWork {

    private Integer version;

    private Map<Integer, AggregateIdTypePair> updatedObjects;

    private Set<DomainEvent> eventsToEmit;

    // Cumulative dependencies of the functionality
    private Map<Integer, Aggregate> currentReadDependencies;

    public UnitOfWork(Integer version) {
        this.updatedObjects = new HashMap<Integer, AggregateIdTypePair>();
        this.eventsToEmit = new HashSet<>();

        setVersion(version);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Collection<AggregateIdTypePair> getUpdatedObjects() {
        return updatedObjects.values();
    }

    // TODO store type in aggregate
    public void addUpdatedObject(Aggregate aggregate, String type) {
        this.updatedObjects.put(aggregate.getAggregateId(), new AggregateIdTypePair(aggregate.getId(), type));
    }

    public Set<DomainEvent> getEventsToEmit() {
        return eventsToEmit;
    }

    public void addEvent(DomainEvent event) {
        this.eventsToEmit.add(event);
    }

    public Map<Integer, Aggregate> getCurrentReadDependencies() {
        return currentReadDependencies;
    }

    public void addCurrentReadDependency(Aggregate dep) {
        if(!this.currentReadDependencies.containsKey(dep.getAggregateId())) {
            this.currentReadDependencies.put(dep.getAggregateId(), dep);
        }
    }

    public boolean hasAggregateDep(Integer aggregateId) {
        return this.currentReadDependencies.containsKey(aggregateId);
    }

    public Aggregate getAggregateDep(Integer aggregateId) {
        return this.currentReadDependencies.get(aggregateId);
    }

    public void addDependency(Integer objAggregateId, Dependency dep) {
        if(this.updatedObjects.containsKey(objAggregateId)) {
            AggregateIdTypePair pair = this.updatedObjects.get(objAggregateId);
            pair.addDependency(dep);
        }
    }


}
