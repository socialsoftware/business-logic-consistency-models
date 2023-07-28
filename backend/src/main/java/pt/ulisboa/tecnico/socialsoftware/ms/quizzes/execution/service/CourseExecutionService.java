package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.tcc.CourseExecutionTCC;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.repository.CausalConsistencyRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.service.CausalConsistencyService;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.service.CourseService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.domain.CourseExecutionStudent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.event.publish.AnonymizeStudentEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.event.publish.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.event.publish.UnerollStudentFromCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.event.publish.UpdateStudentNameEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.tcc.CourseExecutionTCCRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.domain.CourseExecutionCourse;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.execution.dto.CourseExecutionDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.ErrorMessage.*;

@Service
public class CourseExecutionService {
    @Autowired
    private CourseExecutionTCCRepository courseExecutionTCCRepository;

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private CausalConsistencyService causalConsistencyService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CausalConsistencyRepository causalConsistencyRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseExecutionDto addCourseExecutionCausalSnapshot(Integer executionAggregateId, UnitOfWork unitOfWorkWorkService) {
        return new CourseExecutionDto((CourseExecutionTCC) causalConsistencyService.addAggregateCausalSnapshot(executionAggregateId, unitOfWorkWorkService));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseExecutionDto createCourseExecution(CourseExecutionDto courseExecutionDto, UnitOfWork unitOfWork) {
        CourseExecutionCourse courseExecutionCourse = new CourseExecutionCourse(courseService.getAndOrCreateCourseRemote(courseExecutionDto, unitOfWork));

        CourseExecutionTCC courseExecution = new CourseExecutionTCC(aggregateIdGeneratorService.getNewAggregateId(), courseExecutionDto, courseExecutionCourse);

        unitOfWork.registerChanged(courseExecution);
        return new CourseExecutionDto(courseExecution);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<CourseExecutionDto> getAllCausalCourseExecutions(UnitOfWork unitOfWork) {
        return courseExecutionTCCRepository.findCourseExecutionIdsOfAllNonDeleted().stream()
                .map(id -> (CourseExecutionTCC) causalConsistencyService.addAggregateCausalSnapshot(id, unitOfWork))
                .map(CourseExecutionDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWork) {

        CourseExecutionTCC oldCourseExecution = (CourseExecutionTCC) causalConsistencyService.addAggregateCausalSnapshot(executionAggregateId, unitOfWork);
        CourseExecutionTCC newCourseExecution = new CourseExecutionTCC(oldCourseExecution);

        /*
            REMOVE_COURSE_IS_VALID
         */
        Integer numberOfExecutionsOfCourse = Math.toIntExact(getAllCausalCourseExecutions(unitOfWork).stream()
                .filter(ce -> ce.getCourseAggregateId() == newCourseExecution.getExecutionCourse().getCourseAggregateId())
                .count());
        if (numberOfExecutionsOfCourse == 1) {
            throw new TutorException(CANNOT_DELETE_COURSE_EXECUTION);
        }

        newCourseExecution.remove();
        unitOfWork.registerChanged(newCourseExecution);
        unitOfWork.addEvent(new RemoveCourseExecutionEvent(newCourseExecution.getAggregateId()));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void enrollStudent(Integer courseExecutionAggregateId, UserDto userDto, UnitOfWork unitOfWork) {
        CourseExecutionTCC oldCourseExecution = (CourseExecutionTCC) causalConsistencyService.addAggregateCausalSnapshot(courseExecutionAggregateId, unitOfWork);

        CourseExecutionStudent courseExecutionStudent = new CourseExecutionStudent(userDto);
        if (!courseExecutionStudent.isActive()){
            throw new TutorException(ErrorMessage.INACTIVE_USER, courseExecutionStudent.getUserAggregateId());
        }

        CourseExecutionTCC newCourseExecution = new CourseExecutionTCC(oldCourseExecution);
        newCourseExecution.addStudent(courseExecutionStudent);

        unitOfWork.registerChanged(newCourseExecution);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Set<CourseExecutionDto> getCourseExecutionsByUser(Integer userAggregateId, UnitOfWork unitOfWork) {
        return courseExecutionTCCRepository.findAll().stream()
                .map(CourseExecution::getAggregateId)
                .map(aggregateId -> (CourseExecutionTCC) causalConsistencyService.addAggregateCausalSnapshot(aggregateId, unitOfWork))
                .filter(ce -> ce.hasStudent(userAggregateId))
                .map(ce -> new CourseExecutionDto(ce))
                .collect(Collectors.toSet());

    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeStudentFromCourseExecution(Integer courseExecutionAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        CourseExecutionTCC oldCourseExecution = (CourseExecutionTCC) causalConsistencyService.addAggregateCausalSnapshot(courseExecutionAggregateId, unitOfWork);
        CourseExecutionTCC newCourseExecution = new CourseExecutionTCC(oldCourseExecution);
        newCourseExecution.removeStudent(userAggregateId);
        unitOfWork.registerChanged(newCourseExecution);
        unitOfWork.addEvent(new UnerollStudentFromCourseExecutionEvent(courseExecutionAggregateId, userAggregateId));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserDto getStudentByExecutionIdAndUserId(Integer executionAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        CourseExecution courseExecution = (CourseExecutionTCC) causalConsistencyService.addAggregateCausalSnapshot(executionAggregateId, unitOfWork);
        if (!courseExecution.hasStudent(userAggregateId)) {
            throw new TutorException(COURSE_EXECUTION_STUDENT_NOT_FOUND, userAggregateId, executionAggregateId);
        }
        return courseExecution.findStudent(userAggregateId).buildDto();
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void anonymizeStudent(Integer executionAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        CourseExecutionTCC oldExecution = (CourseExecutionTCC) causalConsistencyService.addAggregateCausalSnapshot(executionAggregateId, unitOfWork);
        if (!oldExecution.hasStudent(userAggregateId)) {
            throw new TutorException(COURSE_EXECUTION_STUDENT_NOT_FOUND, userAggregateId, executionAggregateId);
        }
        CourseExecutionTCC newExecution = new CourseExecutionTCC(oldExecution);
        newExecution.findStudent(userAggregateId).anonymize();
        unitOfWork.registerChanged(newExecution);
        unitOfWork.addEvent(new AnonymizeStudentEvent(executionAggregateId, "ANONYMOUS", "ANONYMOUS", userAggregateId));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateExecutionStudentName(Integer executionAggregateId, Integer userAggregateId, String name, UnitOfWork unitOfWork) {
        CourseExecutionTCC oldExecution = (CourseExecutionTCC) causalConsistencyService.addAggregateCausalSnapshot(executionAggregateId, unitOfWork);
        if (!oldExecution.hasStudent(userAggregateId)) {
            throw new TutorException(COURSE_EXECUTION_STUDENT_NOT_FOUND, userAggregateId, executionAggregateId);
        }
        CourseExecutionTCC newExecution = new CourseExecutionTCC(oldExecution);
        newExecution.findStudent(userAggregateId).setName(name);
        unitOfWork.registerChanged(newExecution);
        unitOfWork.addEvent(new UpdateStudentNameEvent(executionAggregateId, userAggregateId, name));
    }

    /************************************************ EVENT PROCESSING ************************************************/


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseExecution removeUser(Integer executionAggregateId, Integer userAggregateId, Integer aggregateEventVersion, UnitOfWork unitOfWork) {
        CourseExecutionTCC oldExecution = (CourseExecutionTCC) causalConsistencyService.addAggregateCausalSnapshot(executionAggregateId, unitOfWork);
        CourseExecutionTCC newExecution = new CourseExecutionTCC(oldExecution);
        newExecution.findStudent(userAggregateId).setState(Aggregate.AggregateState.INACTIVE);
        unitOfWork.registerChanged(newExecution);
        unitOfWork.addEvent(new UnerollStudentFromCourseExecutionEvent(executionAggregateId, userAggregateId));
        return newExecution;
    }
}
