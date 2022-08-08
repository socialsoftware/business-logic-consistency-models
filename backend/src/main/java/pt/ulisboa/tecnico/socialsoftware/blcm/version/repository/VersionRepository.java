package pt.ulisboa.tecnico.socialsoftware.blcm.version.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.version.domain.Version;

public interface VersionRepository extends JpaRepository<Version, Integer> {
}
