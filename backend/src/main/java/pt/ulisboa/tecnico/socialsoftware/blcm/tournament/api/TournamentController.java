package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
public class TournamentController {

    private final Logger logger = LoggerFactory.getLogger(TournamentController.class);


    @Autowired
    private TournamentFunctionalities tournamentFunctionalities;

    @PostMapping(value = "/tournaments/{executionId}")
    public TournamentDto createTournament(@RequestParam Integer userId, @PathVariable int executionId, @RequestParam List<Integer> topicsId, @RequestBody TournamentDto tournamentDto) {
        formatDates(tournamentDto);
        return tournamentFunctionalities.createTournament(userId, executionId, topicsId, tournamentDto);
    }

    private void formatDates(TournamentDto tournamentDto) {
        if (tournamentDto.getStartTime() != null && !DateHandler.isValidDateFormat(tournamentDto.getStartTime()))
            tournamentDto.setStartTime(DateHandler.toISOString(DateHandler.toLocalDateTime(tournamentDto.getStartTime())));

        if (tournamentDto.getEndTime() !=null && !DateHandler.isValidDateFormat(tournamentDto.getEndTime()))
            tournamentDto.setEndTime(DateHandler.toISOString(DateHandler.toLocalDateTime(tournamentDto.getEndTime())));
    }
}
