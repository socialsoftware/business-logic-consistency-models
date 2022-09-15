package pt.ulisboa.tecnico.socialsoftware.blcm.user.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.UserFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import java.util.List;
import java.util.Set;

@RestController
public class UserController {

    @Autowired
    private UserFunctionalities userFunctionalities;

    @PostMapping("/users/create")
    public UserDto createUser(@RequestBody UserDto userDto) {
        return userFunctionalities.createUser(userDto);
    }

    @PostMapping("/executions/{executionAggregateId}/anonymize")
    public void anonymizeCourseExecutionUsers(@PathVariable Integer executionAggregateId) {
        userFunctionalities.anonymizeCourseExecutionUsers(executionAggregateId);
    }

    @PostMapping("/users/{userAggregateId}/executions/add")
    public void addCourseExecution(@PathVariable Integer userAggregateId, @RequestParam Integer executionAggregateId) {
        userFunctionalities.addCourseExecution(userAggregateId, executionAggregateId);

    }

    @GetMapping("/users/{userAggregateId}")
    public UserDto findByAggregateId(@PathVariable Integer userAggregateId) {
        return userFunctionalities.findByAggregateId(userAggregateId);
    }

    @PostMapping("/users/{userAggregateId}/activate")
    public void activateUser(@PathVariable Integer userAggregateId) {
        userFunctionalities.activateUser(userAggregateId);
    }

    @GetMapping("/users/{userAggregateId}/executions")
    public Set<CourseExecutionDto> getUserCourseExecutions(@PathVariable Integer userAggregateId) {
        return userFunctionalities.getUserCourseExecutions(userAggregateId);
    }

    @PostMapping("/users/{userAggregateId}/delete")
    public void deleteUser(@PathVariable Integer userAggregateId) {
        userFunctionalities.deleteUser(userAggregateId);
    }

    @PostMapping("/users/{userAggregateId}/executions/remove")
    public void removeCourseExecutionsFromUser(@PathVariable Integer userAggregateId, @RequestBody List<Integer> courseExecutionsIds) {
        userFunctionalities.removeCourseExecutionsFromUser(userAggregateId, courseExecutionsIds);
    }

    @GetMapping("/users/students")
    public List<UserDto> getStudents() {
        return userFunctionalities.getStudents();
    }

    @GetMapping("/users/teachers")
    public List<UserDto> getTeachers() {
        return userFunctionalities.getTeachers();
    }
}
