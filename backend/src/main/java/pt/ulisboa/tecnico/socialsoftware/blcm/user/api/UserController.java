package pt.ulisboa.tecnico.socialsoftware.blcm.user.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.UserFunctionalities;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserFunctionalities userFunctionalities;

    @PostMapping("/users/create")
    public UserDto createUser(@RequestBody UserDto userDto) {
        return userFunctionalities.createUser(userDto);
    }

    @GetMapping("/users/{userAggregateId}")
    public UserDto findByAggregateId(@PathVariable Integer userAggregateId) {
        return userFunctionalities.findByAggregateId(userAggregateId);
    }

    @PostMapping("/users/{userAggregateId}/activate")
    public void activateUser(@PathVariable Integer userAggregateId) {
        userFunctionalities.activateUser(userAggregateId);
    }

    @PostMapping("/users/{userAggregateId}/delete")
    public void deleteUser(@PathVariable Integer userAggregateId) {
        userFunctionalities.deleteUser(userAggregateId);
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
