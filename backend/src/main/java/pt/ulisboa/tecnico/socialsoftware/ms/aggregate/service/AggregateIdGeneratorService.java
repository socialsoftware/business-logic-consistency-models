package pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.AggregateIdGenerator;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.respository.AggregateIdRepository;

import java.sql.SQLException;

@Service
public class AggregateIdGeneratorService {
    @Autowired
    private AggregateIdRepository aggregateIdRepository;
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Integer getNewAggregateId() {
        AggregateIdGenerator aggregateId = new AggregateIdGenerator();
        aggregateIdRepository.save(aggregateId);
        return aggregateId.getId();
    }
}
