package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.domain.Version;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface VersionRepository extends JpaRepository<Version, Integer> {
}
