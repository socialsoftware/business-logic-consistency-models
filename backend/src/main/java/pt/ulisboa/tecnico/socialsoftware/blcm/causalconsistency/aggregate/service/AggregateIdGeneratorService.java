package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateIdGenerator;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.respository.AggregateIdRepository;

import javax.transaction.Transactional;

@Service
public class AggregateIdGeneratorService {
    @Autowired
    private AggregateIdRepository aggregateIdRepository;

    @Transactional
    public Integer getNewAggregateId() {
        AggregateIdGenerator aggregateId = new AggregateIdGenerator();
        aggregateIdRepository.save(aggregateId);
        return aggregateId.getId();
    }
}
