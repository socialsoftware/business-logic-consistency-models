package pt.ulisboa.tecnico.socialsoftware.blcm.version.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import pt.ulisboa.tecnico.socialsoftware.blcm.version.domain.Version;
import pt.ulisboa.tecnico.socialsoftware.blcm.version.repository.VersionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;

import javax.transaction.Transactional;

@Service
public class VersionService {

    @Autowired
    private VersionRepository versionRepository;

    @Transactional
    public Integer getVersionNumber() {
        Version version = versionRepository.findAll().stream().findFirst().orElseThrow(() -> new TutorException(ErrorMessage.VERSION_MANAGER_DOES_NOT_EXIST));
        return version.getVersionNumber();
    }

    @Transactional
    public void incrementVersionNumber() {
        Version version = versionRepository.findAll().stream().findFirst().orElseThrow(() -> new TutorException(ErrorMessage.VERSION_MANAGER_DOES_NOT_EXIST));
        version.incrementVersion();
        versionRepository.save(version);
    }
}
