package pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.DomainEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.version.service.VersionService;

import javax.persistence.PostLoad;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Component
public class UnitOfWork {

    private Integer version;

    private Set<AggregateIdTypePair> updatedObjects;

    private Set<DomainEvent> eventsToEmit;


    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private VersionService versionService;

    @Autowired
    private EventRepository eventRepository;


    public UnitOfWork() {

    }

    /* executes after all services have been instantiated and all fields have been injected*/
    @PostLoad
    public void init() {
        setVersion(versionService.getVersionNumber());
    }

    @Transactional
    public void commit() {
        Integer newVersion = this.version + 1;
        /* initial commit */
        commitAllObjects(newVersion);

        /* before the transaction ends checks if no other write was made recently */
        while(isTherePossibleConcurrentVersions(this.version)) {
            /* if so gets a new version re-executes commit actions*/
            newVersion = versionService.getVersionNumber();
            commitAllObjects(newVersion + 1);
        }

        this.eventsToEmit.forEach(e -> eventRepository.save(e));
        versionService.incrementVersionNumber();
    }

    private void commitAllObjects(Integer version) {
        this.updatedObjects.forEach(obj -> {
            switch (obj.getType()) {
                case "Tournament":
                    Tournament tournamentToWrite = tournamentRepository.findById(obj.getObjectId())
                            .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, obj.getObjectId())); /* should be aggreateId??*/
                    Tournament concurrentTournament = getConcurrentTournament(tournamentToWrite, version);

                    if(concurrentTournament.getState().equals(DELETED)) {
                        throw new TutorException(TOURNAMENT_DELETED, concurrentTournament.getAggregateId());
                    }
                    /* no need to commit again if as already been committed and no concurrent version exists*/
                    if(!obj.isCommited() || concurrentTournament != null) {
                        Tournament prevTournament = tournamentRepository.findById(obj.getPrevObjectId())
                                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, obj.getPrevObjectId()));
                        commitTournament(prevTournament, tournamentToWrite, concurrentTournament, version);
                    }
                    break;
                case "Quiz":
                    break;
                default:
                    throw new TutorException(INVALID_AGGREGATE_TYPE, obj.getType());
            }
        });
    }

    private void commitTournament(Tournament prevTournament, Tournament tournamentToWrite, Tournament concurrentTournament, Integer version) {
        /* this will likely have to be a loop in the case new concurrent versions appear while this is executing */
        Tournament finalTournament;
        if(concurrentTournament != null) {
            finalTournament = Tournament.merge(prevTournament /*prev*/, concurrentTournament /*v1*/, tournamentToWrite/*v2*/);
        } else {
            finalTournament = new Tournament(tournamentToWrite);
        }

        if(!finalTournament.verifyInvariants()) {
            /* ABORT don't write anything???? */
            throw new TutorException(TOURNAMENT_INVALID, finalTournament.getId());
        }
        finalTournament.setVersion(version);
        finalTournament.setState(ACTIVE);
        finalTournament.setCreationTs(LocalDateTime.now());

        tournamentRepository.save(finalTournament);
    }

    private boolean isTherePossibleConcurrentVersions(Integer currentVersion) {
        return currentVersion == versionService.getVersionNumber();
    }

    private Tournament getConcurrentTournament(Tournament tournament, Integer version) {
        Tournament concurrentTournament = tournamentRepository.findConcurrentVersions(tournament.getAggregateId(), version)
                .stream()
                .findFirst()
                .get();
        return concurrentTournament;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Set<AggregateIdTypePair> getUpdatedObjects() {
        return updatedObjects;
    }

    public void addUpdatedObject(Aggregate aggregate) {
        this.updatedObjects.add(new AggregateIdTypePair(aggregate.getId(), aggregate.getClass().getTypeName()));
    }

    public Set<DomainEvent> getEventsToEmit() {
        return eventsToEmit;
    }

    public void addEvent(DomainEvent event) {
        this.eventsToEmit.add(event);
    }
}
