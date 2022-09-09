package pt.ulisboa.tecnico.socialsoftware.blcm.user.dto;

import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.UserCourseExecution;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDto implements Serializable {
    private Integer aggregateId;

    private String name;

    private String username;

    private String role;

    private boolean active;

    private Integer version;

    private Set<CourseExecutionDto> executions;

    public UserDto() {

    }

    public UserDto(User user) {
        this.aggregateId = user.getAggregateId();
        this.name = user.getName();
        setRole(user.getRole().toString());
        this.username = user.getUsername();
        this.version = user.getVersion();
        setActive(user.isActive());
        setExecutions(user.getCourseExecutions().stream()
                .map(UserCourseExecution::buildDto)
                .collect(Collectors.toSet()));
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Set<CourseExecutionDto> getExecutions() {
        return executions;
    }

    public void setExecutions(Set<CourseExecutionDto> executions) {
        this.executions = executions;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
