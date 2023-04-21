package pt.ulisboa.tecnico.socialsoftware.blcm.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.VersionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.service.CourseService;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.ExecutionCourse;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.ExecutionStudent;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.service.UserService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;

import java.util.List;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Service
public class CourseExecutionFunctionalities {
    private static final Logger logger = LoggerFactory.getLogger(CourseExecutionFunctionalities.class);

    @Autowired
    private VersionService versionService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseExecutionService courseExecutionService;

    @Autowired
    private UserService userService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public CourseExecutionDto createCourseExecution(CourseExecutionDto courseExecutionDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        checkInput(courseExecutionDto);
        ExecutionCourse executionCourse = new ExecutionCourse(courseService.getAndOrCreateCourseRemote(courseExecutionDto, unitOfWork));
        CourseExecutionDto courseExecutionDto1 = courseExecutionService.createCourseExecution(courseExecutionDto, executionCourse, unitOfWork);
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
        unitOfWorkService.commit(unitOfWork);
    }

    public void addStudent(Integer executionAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        UserDto userDto = userService.getCausalUserRemote(userAggregateId, unitOfWork);
        ExecutionStudent executionUser = new ExecutionStudent(userDto);
        courseExecutionService.enrollStudent(executionAggregateId, executionUser, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public Set<CourseExecutionDto> getCourseExecutionsByUser(Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return courseExecutionService.getCourseExecutionsByUser(userAggregateId, unitOfWork);
    }

    public void removeStudentFromCourseExecution(Integer courseExecutionAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        courseExecutionService.removeStudentFromCourseExecution(courseExecutionAggregateId, userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void anonymizeStudent(Integer executionAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        courseExecutionService.anonymizeStudent(executionAggregateId, userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void updateExecutionStudentName(Integer executionAggregateId, Integer userAggregateId ,UserDto userDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        logger.info("START EXECUTION FUNCTIONALITY: updateExecutionStudentName with version {}", unitOfWork.getVersion());

        if (userDto.getName() == null) {
            throw new TutorException(USER_MISSING_NAME);
        }

        courseExecutionService.updateExecutionStudentName(executionAggregateId, userAggregateId, userDto.getName(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);

        logger.info("END EXECUTION FUNCTIONALITY: updateExecutionStudentName with version {}", versionService.getVersionNumber());
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
