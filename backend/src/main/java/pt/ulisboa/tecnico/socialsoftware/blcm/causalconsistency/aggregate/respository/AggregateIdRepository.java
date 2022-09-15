package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateIdGenerator;

public interface AggregateIdRepository extends JpaRepository<AggregateIdGenerator, Integer> {
}
