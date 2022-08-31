package pt.ulisboa.tecnico.socialsoftware.blcm.execution.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionFunctionalities;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;

import java.util.List;
import java.util.Set;

@RestController
public class CourseExecutionController {

    @Autowired
    private CourseExecutionFunctionalities courseExecutionFunctionalities;

    @PostMapping(value = "/executions/{courseId}")
    public CourseExecutionDto createTournament(@RequestBody CourseExecutionDto executionDto) {
        formatDates(executionDto);
        return courseExecutionFunctionalities.createCourseExecution(executionDto);
    }

    private void formatDates(CourseExecutionDto courseExecutionDto) {
        if (courseExecutionDto.getEndDate() != null && !DateHandler.isValidDateFormat(courseExecutionDto.getEndDate()))
            courseExecutionDto.setEndDate(DateHandler.toISOString(DateHandler.toLocalDateTime(courseExecutionDto.getEndDate())));
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
