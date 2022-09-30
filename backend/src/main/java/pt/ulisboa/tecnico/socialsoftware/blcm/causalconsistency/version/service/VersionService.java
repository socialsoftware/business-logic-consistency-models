package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.domain.Version;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.repository.VersionRepository;

import java.sql.SQLException;
import java.util.Optional;


@Service
public class VersionService {

    @Autowired
    private VersionRepository versionRepository;

    /* cannot allow two transactions to get the same version number*/
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Integer getVersionNumber() {
        Optional<Version> versionOp = versionRepository.findAll().stream().findFirst();
        Version version;
        if(versionOp.isEmpty()) {
            version = new Version();
            versionRepository.save(version);
        } else {
            version = versionOp.get();
        }
        version.incrementVersion();
        return version.getVersionNumber();
    }

    /*@Transactional
    public void incrementVersionNumber() {
        Version version = versionRepository.findAll().stream().findFirst().orElseThrow(() -> new TutorException(ErrorMessage.VERSION_MANAGER_DOES_NOT_EXIST));
        version.incrementVersion();
        versionRepository.save(version);
    }*/
}
