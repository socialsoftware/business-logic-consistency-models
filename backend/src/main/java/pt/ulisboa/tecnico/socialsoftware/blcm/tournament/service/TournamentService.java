package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.TournamentCreationEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.INACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.TOURNAMENT_NOT_FOUND;

@Service
public class TournamentService {
    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Transactional
    public TournamentDto createTournament(TournamentDto tournamentDto, TournamentCreator creator,
                                          TournamentCourseExecution courseExecution, Set<TournamentTopic> topics,
                                          TournamentQuiz quiz, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        Tournament tournament = new Tournament(aggregateId, tournamentDto, creator, courseExecution, topics, quiz, unitOfWork.getVersion()); /* should the skeleton creation be part of the functionality?? */
        tournamentRepository.save(tournament);
        unitOfWork.addUpdatedObject(tournament);
        unitOfWork.addEvent(new TournamentCreationEvent(tournament));
        return new TournamentDto(tournament);
    }

    @Transactional
    public TournamentDto getTournament(Integer aggregateId, UnitOfWork unitOfWork) {
        Tournament tournament = tournamentRepository.findByAggregateIdAndVersion(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, aggregateId));
        if(tournament.getState().equals(INACTIVE) || tournament.getState().equals(DELETED)) {
            throw new TutorException(TOURNAMENT_NOT_FOUND, aggregateId);
        }

        return new TournamentDto(tournament);
    }

    @Transactional
    public void joinTournament(Integer tournamentAggregateId, TournamentParticipant tournamentParticipant, UnitOfWork unitOfWork) {
        Tournament tournament = tournamentRepository.findByAggregateIdAndVersion(tournamentAggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentAggregateId));
        Tournament newTournamentVersion = new Tournament(tournament);
        newTournamentVersion.addParticipant(tournamentParticipant);
        unitOfWork.addUpdatedObject(newTournamentVersion);
    }




    @Transactional
    public void anonymizeUser(Integer userAggregateId, UnitOfWork unitOfWork) {
        Set<Tournament> allTournaments = findAllTournamentByVersion(unitOfWork.getVersion());
        boolean update1 = false;
        boolean update2 = false;
        for(Tournament t : allTournaments) {
            update1 = false;
            update2 = false;
            Tournament newTournament = new Tournament(t);
            TournamentCreator creator = t.getCreator();
            if(creator.getAggregateId() == userAggregateId) {
                creator.setName("ANONYMOUS");
                creator.setName("ANONYMOUS");
                update1 = true;
                newTournament.setCreator(creator);
            }

            /*maybe not needed to create new hashset*/
            Set<TournamentParticipant> participants = new HashSet<>(t.getParticipants());
            for(TournamentParticipant tp : participants) {
                if(tp.getAggregateId() == userAggregateId) {
                    update2 = true;
                    tp.setName("ANONYMOUS");
                    tp.setName("ANONYMOUS");
                }
            }

            if(update2) {
                newTournament.setParticipants(participants);
            }

            if(update1 || update2) {
                unitOfWork.addUpdatedObject(newTournament);
            }
        }
    }

    private Set<Tournament> findAllTournamentByVersion(Integer version) {
        /*get all tournaments latest versions inferior to the given version*/
        Set<Tournament> tournaments = tournamentRepository.findAll()
                .stream()
                .collect(Collectors.toSet());
        Map<Integer, Tournament> tournamentPerAggregateId = new HashMap<>();
        for(Tournament t : tournaments) {
            if (!tournamentPerAggregateId.containsKey(t.getAggregateId())) {
                tournamentPerAggregateId.put(t.getAggregateId(), t);
            } else {
                if(tournamentPerAggregateId.get(t.getAggregateId()).getCreationTs().isBefore(t.getCreationTs())) {
                    tournamentPerAggregateId.put(t.getAggregateId(), t);
                }
            }
        }
        return (Set<Tournament>) tournamentPerAggregateId.values();
    }

}
