package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;

import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface UnitOfWorkRepository extends JpaRepository<UnitOfWork, Integer> {

    @Query(value = "SELECT * FROM unit_of_works uow, unit_of_work_aggregate_ids uowi WHERE uow.id = uowi.unit_of_work_id AND uowi.aggregate_ids = :aggregateId AND uow.version < :version", nativeQuery = true)
    Set<UnitOfWork> findRunningTransactions(Integer aggregateId, Integer version);

}
