package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventUtils {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProcessedEventsRepository processedEventsRepository;

    public List<Event> getEmittedEvents(String eventType) {
        return eventRepository.findAll()
                .stream()
                .filter(e -> eventType.equals(e.getType()))
                .distinct()
                .sorted(Comparator.comparing(Event::getTs).reversed())
                .collect(Collectors.toList());
    }

    public ProcessedEvents getTournamentProcessedEvents(String eventType, Integer aggregateId) {
        return processedEventsRepository.findAll().stream()
                .filter(pe -> aggregateId.equals(pe.getAggregateId()))
                .filter(pe -> eventType.equals(pe.getEventType()))
                .findFirst()
                .orElse(new ProcessedEvents(eventType, aggregateId));
    }
}
