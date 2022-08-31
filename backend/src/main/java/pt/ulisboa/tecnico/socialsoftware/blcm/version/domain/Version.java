package pt.ulisboa.tecnico.socialsoftware.blcm.version.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Version {

    @Id
    private Integer id;

    @Column(name = "version_number")
    private Integer versionNumber = 1;

    public Version() {
        versionNumber = 1;
    }

    public Integer getVersionNumber() {
        return this.versionNumber;
    }

    public void incrementVersion() {
        versionNumber++;
    }

}
