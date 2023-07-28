package pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.repository.CausalConsistencyRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.TutorException;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.ErrorMessage.*;

@Service
public class CausalConsistencyService {
    @Autowired
    private CausalConsistencyRepository causalConsistencyRepository;
    @Autowired
    private EventRepository eventRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Aggregate addAggregateCausalSnapshot(Integer aggregateId, UnitOfWork unitOfWork) {
        Aggregate aggregate = causalConsistencyRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(AGGREGATE_NOT_FOUND, aggregateId));

        if (aggregate.getState() == Aggregate.AggregateState.DELETED) {
            throw new TutorException(AGGREGATE_DELETED, aggregate.getAggregateType().toString(), aggregate.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();

        unitOfWork.addToCausalSnapshot(aggregate, allEvents);
        return aggregate;
    }

}
