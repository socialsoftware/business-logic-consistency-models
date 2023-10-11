package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.tournament.events.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.microservices.tournament.events.TournamentEventHandling;

@RestController
public class TournamentEventController {
    @Autowired
    private TournamentEventHandling tournamentEventHandling;

    @PostMapping("/events/topic/update")
    public void processUpdateTopic() throws Throwable {
        tournamentEventHandling.handleUpdateTopicEvents();
    }
}
