package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.tcc.CourseExecutionTCC;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.service.CausalConsistencyService;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.service.UserService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.TutorException;

import java.util.List;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.ErrorMessage.*;

@Service
public class CourseExecutionFunctionalities {
    @Autowired
    private CausalConsistencyService causalConsistencyService;

    @Autowired
    private CourseExecutionService courseExecutionService;

    @Autowired
    private UserService userService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public CourseExecutionDto createCourseExecution(CourseExecutionDto courseExecutionDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        checkInput(courseExecutionDto);
        CourseExecutionDto courseExecutionDto1 = courseExecutionService.createCourseExecution(courseExecutionDto, unitOfWork);
        unitOfWorkService.commit(unitOfWork);

        return courseExecutionDto1;
    }

    public CourseExecutionDto getCourseExecutionByAggregateId(Integer executionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        CourseExecution courseExecution = (CourseExecutionTCC) causalConsistencyService.addAggregateCausalSnapshot(executionAggregateId, unitOfWork);
        return  new CourseExecutionDto(courseExecution);
    }

    public List<CourseExecutionDto> getCourseExecutions() {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        return courseExecutionService.getAllCausalCourseExecutions(unitOfWork);
    }

    public void removeCourseExecution(Integer executionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());

        courseExecutionService.removeCourseExecution(executionAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void addStudent(Integer executionAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());

        UserDto userDto = userService.getCausalUserRemote(userAggregateId, unitOfWork);
        courseExecutionService.enrollStudent(executionAggregateId, userDto, unitOfWork);

        unitOfWorkService.commit(unitOfWork);
    }

    public Set<CourseExecutionDto> getCourseExecutionsByUser(Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        return courseExecutionService.getCourseExecutionsByUser(userAggregateId, unitOfWork);
    }

    public void removeStudentFromCourseExecution(Integer courseExecutionAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());

        courseExecutionService.removeStudentFromCourseExecution(courseExecutionAggregateId, userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void anonymizeStudent(Integer executionAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());

        courseExecutionService.anonymizeStudent(executionAggregateId, userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void updateExecutionStudentName(Integer executionAggregateId, Integer userAggregateId ,UserDto userDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());

        if (userDto.getName() == null) {
            throw new TutorException(USER_MISSING_NAME);
        }

        courseExecutionService.updateExecutionStudentName(executionAggregateId, userAggregateId, userDto.getName(), unitOfWork);
        unitOfWorkService.commit(unitOfWork);
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
