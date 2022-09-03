package pt.ulisboa.tecnico.socialsoftware.blcm.execution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.CourseService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.ExecutionCourse;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.Dependency;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWorkService;

import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Service
public class CourseExecutionFunctionalities {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseExecutionService courseExecutionService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public CourseExecutionDto createCourseExecution(CourseExecutionDto courseExecutionDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();

        checkInput(courseExecutionDto);

        ExecutionCourse executionCourse = new ExecutionCourse(courseService.getAndOrCreateCourseRemote(courseExecutionDto, unitOfWork));

        CourseExecutionDto courseExecutionDto1 = courseExecutionService.createCourseExecution(courseExecutionDto, executionCourse, unitOfWork);

        //unitOfWork.addDependency(courseExecutionDto.getAggregateId(), new Dependency(executionCourse.getAggregateId(), "Course", unitOfWork.getVersion()));
        //unitOfWork.addDependency(executionCourse.getAggregateId(), new Dependency(courseExecutionDto.getAggregateId(), "CourseExecution", unitOfWork.getVersion()));

        // TODO replace with UoW commit after fixing it
        unitOfWorkService.commit(unitOfWork);

        return courseExecutionDto1;
    }

    public CourseExecutionDto getCourseExecutionByAggregateId(Integer executionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        CourseExecution courseExecution = courseExecutionService.getCausalCourseExecutionLocal(executionAggregateId, unitOfWork);
        return  new CourseExecutionDto(courseExecution);
    }

    public List<CourseExecutionDto> getCourseExecutions() {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return courseExecutionService.getAllCausalCourseExecutions(unitOfWork);
    }

    public void removeCourseExecution(Integer executionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        courseExecutionService.removeCourseExecution(executionAggregateId, unitOfWork);

    }

    private void checkInput(CourseExecutionDto courseExecutionDto) {
        if (courseExecutionDto.getAcronym() == null) {
            throw new TutorException(COURSE_EXECUTION_MISSING_ACRONYM);
        }
        if (courseExecutionDto.getAcademicTerm() == null) {
            throw new TutorException(COURSE_EXECUTION_MISSING_ACADEMIC_TERM);
        }
        if (courseExecutionDto.getEndDate() == null) {
            throw new TutorException(COURSE_EXECUTION_MISSING_END_DATE);
        }

    }

}
