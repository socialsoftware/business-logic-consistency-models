package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.TournamentCreationEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.TOURNAMENT_DELETED;
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
                                          TournamentQuiz quiz, UnitOfWork unitOfWorkWorkService) {


        /* add the dependencies to the tournament here */
        /* in the unit of work manage the dependencies on commit time*/
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        Tournament tournament = new Tournament(aggregateId, tournamentDto, creator, courseExecution, topics, quiz, unitOfWorkWorkService.getVersion()); /* should the skeleton creation be part of the functionality?? */
        unitOfWorkWorkService.addUpdatedObject(tournament);
        unitOfWorkWorkService.addEvent(new TournamentCreationEvent(tournament));
        return new TournamentDto(tournament);
    }

    // intended for requests from external functionalities
    @Transactional
    public TournamentDto getCausalTournamentRemote(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        return new TournamentDto(getCausalTournamentLocal(aggregateId, unitOfWorkWorkService));
    }

    // intended for requests from local functionalities

    public Tournament getCausalTournamentLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        Tournament tournament = tournamentRepository.findByAggregateIdAndVersion(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, aggregateId));

        if(tournament.getState().equals(DELETED)) {
            throw new TutorException(TOURNAMENT_DELETED, tournament.getAggregateId());
        }

        unitOfWork.checkDependencies(tournament);
        return tournament;
    }

    @Transactional
    public void joinTournament(Integer tournamentAggregateId, TournamentParticipant tournamentParticipant, UnitOfWork unitOfWorkWorkService) {
        Tournament tournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWorkWorkService);
        Tournament newTournamentVersion = new Tournament(tournament);
        newTournamentVersion.addParticipant(tournamentParticipant);
        unitOfWorkWorkService.addUpdatedObject(newTournamentVersion);
    }



    /* discuss if the processing for all tournaments should be done at the same time */
    @Transactional
    public void anonymizeUser(Integer userAggregateId, String name, String username, UnitOfWork unitOfWork) {
        Set<Tournament> allTournaments = findAllTournamentByVersion(unitOfWork);
        boolean update1 = false;
        boolean update2 = false;
        for(Tournament t : allTournaments) {
            update1 = false;
            update2 = false;
            Tournament newTournament = new Tournament(t);
            TournamentCreator creator = t.getCreator();
            if(creator.getAggregateId() == userAggregateId) {
                creator.setName(name);
                creator.setName(username);
                update1 = true;
                newTournament.setCreator(creator);
            }

            /*maybe not needed to create new hashset*/
            Set<TournamentParticipant> participants = new HashSet<>(t.getParticipants());
            for(TournamentParticipant tp : participants) {
                if(tp.getAggregateId() == userAggregateId) {
                    update2 = true;
                    tp.setName(name);
                    tp.setName(username);
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

    private Set<Tournament> findAllTournamentByVersion(UnitOfWork unitOfWork) {
        Set<Tournament> tournaments = tournamentRepository.findAll()
                .stream()
                .filter(t -> t.getVersion() <= unitOfWork.getVersion())
                .collect(Collectors.toSet());

        Map<Integer, Tournament> tournamentPerAggregateId = new HashMap<>();
        for(Tournament t : tournaments) {
            if(t.getState().equals(DELETED)) {
                throw new TutorException(TOURNAMENT_DELETED, t.getAggregateId());
            }
            unitOfWork.checkDependencies(t);

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

    @Transactional
    public TournamentDto updateTournament(TournamentDto tournamentDto, Set<TournamentTopic> tournamentTopics, UnitOfWork unitOfWorkWorkService) {
        /* check how the update is actually done */
        Tournament oldTournament = getCausalTournamentLocal(tournamentDto.getAggregateId(), unitOfWorkWorkService);

        Tournament newTournament = new Tournament(oldTournament);
        newTournament.setStartTime(DateHandler.toLocalDateTime(tournamentDto.getStartTime()));
        newTournament.setEndTime(DateHandler.toLocalDateTime(tournamentDto.getEndTime()));
        newTournament.setNumberOfQuestions(tournamentDto.getNumberOfQuestions());
        newTournament.setTopics(tournamentTopics);
        unitOfWorkWorkService.addUpdatedObject(newTournament);
        return new TournamentDto(newTournament);
    }

    @Transactional
    public List<TournamentDto> getTournamentsForCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWorkWorkService) {
        /*switch this to query???*/
        return findAllTournamentByVersion(unitOfWorkWorkService).stream()
                .filter(t -> t.getCourseExecution().getAggregateId() == executionAggregateId)
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TournamentDto> getOpenedTournamentsForCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWorkWorkService) {
        /*switch this to query???*/
        LocalDateTime now = LocalDateTime.now();
        return findAllTournamentByVersion(unitOfWorkWorkService).stream()
                .filter(t -> t.getCourseExecution().getAggregateId() == executionAggregateId)
                .filter(t -> now.isBefore(t.getEndTime()))
                .filter(t -> !t.isCancelled())
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TournamentDto> getClosedTournamentsForCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWorkWorkService) {
        /*switch this to query???*/
        LocalDateTime now = LocalDateTime.now();
        return findAllTournamentByVersion(unitOfWorkWorkService).stream()
                .filter(t -> t.getCourseExecution().getAggregateId() == executionAggregateId)
                .filter(t -> now.isAfter(t.getEndTime()))
                .filter(t -> !t.isCancelled())
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void leaveTournament(Integer tournamentAggregateId, Integer userAggregateId, UnitOfWork unitOfWorkWorkService) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWorkWorkService);
        Tournament newTournament = new Tournament(oldTournament);
        TournamentParticipant participantToRemove = newTournament.findParticipant(userAggregateId);
        newTournament.removeParticipant(participantToRemove);
        unitOfWorkWorkService.addUpdatedObject(newTournament);
    }

    @Transactional
    public void solveQuiz(Integer tournamentAggregateId, Integer userAggregateId, UnitOfWork unitOfWorkWorkService) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWorkWorkService);
        Tournament newTournament = new Tournament(oldTournament);
        newTournament.findParticipant(userAggregateId).answerQuiz();
        unitOfWorkWorkService.addUpdatedObject(newTournament);
    }

    @Transactional
    public void cancelTournament(Integer tournamentAggregateId, UnitOfWork unitOfWorkWorkService) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWorkWorkService);
        Tournament newTournament = new Tournament(oldTournament);
        newTournament.cancel();
        unitOfWorkWorkService.addUpdatedObject(newTournament);
    }

    public void remove(Integer tournamentAggregateId, UnitOfWork unitOfWorkWorkService) {
        Tournament oldTournament = getCausalTournamentLocal(tournamentAggregateId, unitOfWorkWorkService);
        Tournament newTournament = new Tournament(oldTournament);
        newTournament.remove();
    }
}
