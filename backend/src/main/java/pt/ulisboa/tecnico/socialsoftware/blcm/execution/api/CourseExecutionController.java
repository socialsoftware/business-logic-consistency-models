package pt.ulisboa.tecnico.socialsoftware.blcm.execution.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import java.util.List;

@RestController
public class CourseExecutionController {

    @Autowired
    private CourseExecutionFunctionalities courseExecutionFunctionalities;

    @PostMapping(value = "/executions/create")
    public CourseExecutionDto createTournament(@RequestBody CourseExecutionDto executionDto) {
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
}
