package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Version {

    @Id
    @GeneratedValue
    private Integer id;

    // represents the version of the last committed transaction in the system.
    @Column(name = "version_number")
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
