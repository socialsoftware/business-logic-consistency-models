package pt.ulisboa.tecnico.socialsoftware.blcm.tournament.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.AnonymizeUserEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.TournamentFunctionalities;

import java.util.Set;

@Component
public class TournamentEventDetection {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TournamentFunctionalities tournamentFunctionalities;

    @Autowired
    private ProcessedAnonymizeUserEventsRepository processedAnonymizeUserEventsRepository;

    @Scheduled(cron = "*/10 * * * * *")
    public void detectAnonymizeUserEvents() {
        /*System.out.println("Processing anonymize user events");
        ProcessedAnonymizeUserEvents lastProcessedEvent = processedAnonymizeUserEventsRepository.findAll().stream().findFirst().get();

        if(lastProcessedEvent == null) {*/
            /*creates the entry if it does not exist*/
         /*   lastProcessedEvent = new ProcessedAnonymizeUserEvents(0);
        }

        Set<AnonymizeUserEvent> events = eventRepository.getEvents("ANONYMIZE_USER", lastProcessedEvent.getLastProcessed());
        Integer newLastProcessedId = 0;
        for(AnonymizeUserEvent e : events) {
            tournamentFunctionalities.anonymizeUser(e.getUserAggregateId());
            newLastProcessedId = e.getId();
        }

        lastProcessedEvent.setLastProcessed(newLastProcessedId);
        processedAnonymizeUserEventsRepository.save(lastProcessedEvent);
*/
    }
}
