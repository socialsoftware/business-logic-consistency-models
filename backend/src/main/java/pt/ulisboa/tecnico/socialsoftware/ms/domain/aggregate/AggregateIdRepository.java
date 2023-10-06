package pt.ulisboa.tecnico.socialsoftware.ms.domain.aggregate;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.domain.aggregate.AggregateIdGenerator;

public interface AggregateIdRepository extends JpaRepository<AggregateIdGenerator, Integer> {
}
