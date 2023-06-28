package pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.aggregate.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.aggregate.domain.Aggregate;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface AggregateRepository extends JpaRepository<Aggregate, Integer> {

}
