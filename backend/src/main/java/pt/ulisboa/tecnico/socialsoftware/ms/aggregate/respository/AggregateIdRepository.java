package pt.ulisboa.tecnico.socialsoftware.ms.aggregate.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.AggregateIdGenerator;

import java.util.Optional;

public interface AggregateIdRepository extends JpaRepository<AggregateIdGenerator, Integer> {
}
