package pt.ulisboa.tecnico.socialsoftware.blcm.user.dto;

import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;

public class UserDto {
    private Integer aggregateId;

    private String name;

    private String username;

    private Integer version;

    public UserDto() {

    }

    public UserDto(User user) {
        this.aggregateId = user.getAggregateId();
        this.name = user.getName();
        this.username = user.getUsername();
        this.version = user.getVersion();
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
