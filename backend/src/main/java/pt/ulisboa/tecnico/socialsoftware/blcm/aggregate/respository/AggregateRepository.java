package pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface AggregateRepository extends JpaRepository<Aggregate, Integer> {

}
