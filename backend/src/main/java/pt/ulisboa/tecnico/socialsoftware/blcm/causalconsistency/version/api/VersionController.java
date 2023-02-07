package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.service.VersionService;

@RestController
public class VersionController {
    @Autowired
    VersionService versionService;

    @PostMapping(value = "/versions/increment")
    public void incrementVersion() {
        versionService.incrementAndGetVersionNumber();
    }
    @PostMapping(value = "/versions/decrement")
    public void decrementVersion() {
        versionService.decrementVersionNumber();
    }

}
