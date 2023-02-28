package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Version {
    @Id
    @GeneratedValue
    private Integer id;

    // represents the version of the last committed transaction in the system.
    private Integer versionNumber;

    public Version() {
        this.versionNumber = 1;
    }

    public Integer getVersionNumber() {
        return this.versionNumber;
    }

    public void incrementVersion() {
        versionNumber++;
    }

    public void decrementVersion() {
        versionNumber--;
    }
}
