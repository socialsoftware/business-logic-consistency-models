package pt.ulisboa.tecnico.socialsoftware.blcm.execution.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;

import java.util.List;
import java.util.Set;

@RestController
public class CourseExecutionController {

    @Autowired
    private CourseExecutionFunctionalities courseExecutionFunctionalities;

    @PostMapping(value = "/executions/create")
    public CourseExecutionDto createCourseExecution(@RequestBody CourseExecutionDto executionDto) {
        CourseExecutionDto courseExecutionDto = courseExecutionFunctionalities.createCourseExecution(executionDto);
        return courseExecutionDto;
    }

    @GetMapping(value = "/executions/{executionAggregateId}")
    public CourseExecutionDto getCourseExecutionByAggregateId(@PathVariable Integer executionAggregateId) {
        return courseExecutionFunctionalities.getCourseExecutionByAggregateId(executionAggregateId);
    }

    @GetMapping(value = "/executions")
    public List<CourseExecutionDto> getCourseExecutions() {
        return courseExecutionFunctionalities.getCourseExecutions();
    }

    @PostMapping(value = "/executions/{executionAggregateId}/delete")
    public void removeCourseExecution(@PathVariable Integer executionAggregateId) {
        courseExecutionFunctionalities.removeCourseExecution(executionAggregateId);
    }

    @PostMapping("/executions/{executionAggregateId}/students/add")
    public void enrollStudent(@PathVariable Integer executionAggregateId, @RequestParam Integer userAggregateId) {
        courseExecutionFunctionalities.addCourseExecution(executionAggregateId, userAggregateId);
    }

    @GetMapping("/users/{userAggregateId}/executions")
    public Set<CourseExecutionDto> getUserCourseExecutions(@PathVariable Integer userAggregateId) {
        return courseExecutionFunctionalities.getCourseExecutionsByUser(userAggregateId);
    }

    @PostMapping("/executions/{executionAggregateId}/students/remove")
    public void removeStudentFromCourseExecution(@PathVariable Integer executionAggregateId, @RequestParam Integer userAggregateId) {
        courseExecutionFunctionalities.removeStudentFromCourseExecution(executionAggregateId, userAggregateId);
    }

    @PostMapping("/executions/{executionAggregateId}/anonymize")
    public void anonymizeCourseStudent(@PathVariable Integer executionAggregateId, @RequestParam Integer userAggregateId) {
        courseExecutionFunctionalities.anonymizeStudent(executionAggregateId, userAggregateId);
    }
}
