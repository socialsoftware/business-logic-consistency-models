package pt.ulisboa.tecnico.socialsoftware.blcm.user.domain;

import org.apache.commons.collections4.SetUtils;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.AggregateType;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipant;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.Dependency;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.AggregateType.COURSE_EXECUTION;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Entity
@Table(name = "users")
public class User extends Aggregate {

    @ManyToOne(fetch = FetchType.LAZY)
    private User prev;

    @Column
    private String name;

    @Column
    private String username;

    @Enumerated(EnumType.STRING)
    private Role role;


    @Column(columnDefinition = "boolean default false")
    private Boolean active;

    @ElementCollection
    private Set<UserCourseExecution> courseExecutions;

    public User() {

    }

    public User(User otherUser) {
        super(otherUser.getAggregateId());
        setId(null);
        setName(otherUser.getName());
        setUsername(otherUser.getUsername());
        setRole(otherUser.getRole());
        setState(AggregateState.ACTIVE);
        setPrev(otherUser);
        setActive(otherUser.isActive());
        setCourseExecutions(new HashSet<>(otherUser.getCourseExecutions()));

    }

    public User(Integer aggregateId, Integer version, UserDto userDto) {
        super(aggregateId, version);
        setName(userDto.getName());
        setUsername(userDto.getUsername());
        setRole(Role.valueOf(userDto.getRole()));
        setActive(false);
        setCourseExecutions(new HashSet<>());
    }

    public void anonymize() {
        setName("ANONYMOUS");
        setUsername("ANONYMOUS");
    }

    public void remove() {
        if(isActive()) {
            throw new TutorException(USER_ACTIVE, this.getAggregateId());
        }
        setState(AggregateState.DELETED);
    }



    public void removeCourseExecution(UserCourseExecution userCourseExecution) {
        this.courseExecutions.remove(userCourseExecution);
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

    public void setUsername(String userName) {
        this.username = userName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }



    public Set<UserCourseExecution> getCourseExecutions() {
        return courseExecutions;
    }

    public void setCourseExecutions(Set<UserCourseExecution> courseExecutions) {
        this.courseExecutions = courseExecutions;
    }

    public void addCourseExecution(UserCourseExecution userCourseExecution) {
        this.courseExecutions.add(userCourseExecution);
    }

    @Override
    public boolean verifyInvariants() {
        return true;
    }

    @Override
    public Aggregate getPrev() {
        return this.prev;
    }

    public void setPrev(User prev) {
        this.prev = prev;
    }

    @Override
    public Aggregate merge(Aggregate other) {
        User v1 = this;
        if(!(other instanceof User)) {
            throw new TutorException(ErrorMessage.USER_MERGE_FAILURE, prev.getAggregateId());
        }
        User v2 = (User)other;
        User prev = (User)(this.getPrev());

        /* if there is an already concurrent version which is deleted this should not execute*/
        if(v1.getState().equals(DELETED)) {
            throw new TutorException(USER_DELETED, v1.getAggregateId());
        }

        Set<String> v1ChangedFields = getChangedFields(prev, v1);
        Set<String> v2ChangedFields = getChangedFields(prev, v2);

        /* only course executions are incremental */
        if(!(v1ChangedFields.contains("course executions") && v1ChangedFields.size() == 1 && v2ChangedFields.contains("course executions") && v2ChangedFields.size() == 1)) {
            throw new TutorException(ErrorMessage.USER_MERGE_FAILURE, prev.getAggregateId());
        }


        User mergedUser = this;


        if(v1ChangedFields.contains("course executions") || v2ChangedFields.contains("course executions")) {

            Set<UserCourseExecution> addedCourseExecutions =  SetUtils.union(
                    SetUtils.difference(v1.getCourseExecutions(), prev.getCourseExecutions()),
                    SetUtils.difference(v2.getCourseExecutions(), prev.getCourseExecutions())
            );

            Set<UserCourseExecution> removedCourseExecutions = SetUtils.union(
                    SetUtils.difference(prev.getCourseExecutions(), v1.getCourseExecutions()),
                    SetUtils.difference(prev.getCourseExecutions(), v2.getCourseExecutions())
            );


            mergedUser.setCourseExecutions(SetUtils.union(SetUtils.difference(prev.getCourseExecutions(), removedCourseExecutions), addedCourseExecutions));

            /*for(Aggregate dep : v2.getDependencies().values()){
                if (!mergedUser.getDependencies().containsKey(dep.getAggregateId()) && dep instanceof User) {
                    // TODO: create method to allow adding individual dependencies
                    mergedUser.getDependencies().put(dep.getAggregateId(), dep);
                }
            }*/

        }

        return mergedUser;
    }



    private static Set<String> getChangedFields(User prev, User v) {
        Set<String> v1ChangedFields = new HashSet<>();
        if(!prev.getRole().equals(v.getRole())) {
            v1ChangedFields.add("role");
        }

        if(!prev.getName().equals(v.getName())) {
            v1ChangedFields.add("name");
        }

        if(!prev.getUsername().equals(v.getUsername())) {
            v1ChangedFields.add("username");
        }

        if(!prev.isActive().equals(v.isActive())) {
            v1ChangedFields.add("active");
        }

        if(!prev.getCourseExecutions().equals(v.getCourseExecutions())) {
            v1ChangedFields.add("course executions");
        }



        return v1ChangedFields;
    }

    @Override
    public Map<Integer, Dependency> getDependenciesMap() {
        Map<Integer, Dependency> depMap = new HashMap<>();
        this.courseExecutions.forEach(ce -> {
            depMap.put(ce.getAggregateId(), new Dependency(ce.getAggregateId(), COURSE_EXECUTION, ce.getVersion()));
        });
        return depMap;
    }

}
