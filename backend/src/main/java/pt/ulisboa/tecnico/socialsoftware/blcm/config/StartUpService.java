package pt.ulisboa.tecnico.socialsoftware.blcm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.events.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventRepository;

import java.sql.SQLException;

@Service
public class StartUpService {

    @Autowired
    private EventRepository eventRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void clearEvents() {
        eventRepository.deleteAll();
    }
}
