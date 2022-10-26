package pt.ulisboa.tecnico.socialsoftware.blcm.execution.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEventsRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.ExecutionCourse;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.ExecutionStudent;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.service.VersionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.INACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Service
public class CourseExecutionService {
    @Autowired
    private VersionService versionService;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProcessedEventsRepository processedEventsRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseExecutionDto getCausalCourseExecutionRemote(Integer executionAggregateId, UnitOfWork unitOfWorkWorkService) {
        return new CourseExecutionDto(getCausalCourseExecutionLocal(executionAggregateId, unitOfWorkWorkService));
    }



    // intended for requests from local functionalities
    public CourseExecution getCausalCourseExecutionLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        CourseExecution execution = courseExecutionRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, aggregateId));

        if(execution.getState() == DELETED) {
            throw new TutorException(COURSE_EXECUTION_DELETED, execution.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();
        unitOfWork.addToCausalSnapshot(execution, allEvents);
        return execution;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseExecutionDto createCourseExecution(CourseExecutionDto courseExecutionDto, ExecutionCourse executionCourse, UnitOfWork unitOfWork) {
        CourseExecution courseExecution = new CourseExecution(aggregateIdGeneratorService.getNewAggregateId(), courseExecutionDto, executionCourse);

        unitOfWork.registerChanged(courseExecution);
        return new CourseExecutionDto(courseExecution);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<CourseExecutionDto> getAllCausalCourseExecutions(UnitOfWork unitOfWork) {
        return courseExecutionRepository.findAllNonDeleted().stream()
                .map(CourseExecution::getAggregateId)
                .distinct()
                .map(id -> getCausalCourseExecutionLocal(id, unitOfWork))
                .map(CourseExecutionDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWork) {

        CourseExecution oldCourseExecution = getCausalCourseExecutionLocal(executionAggregateId, unitOfWork);
        CourseExecution newCourseExecution = new CourseExecution(oldCourseExecution);

        /*
            REMOVE_COURSE_IS_VALID
         */
        Integer numberOfExecutionsOfCourse = Math.toIntExact(getAllCausalCourseExecutions(unitOfWork).stream()
                .filter(ce -> ce.getCourseAggregateId() == newCourseExecution.getCourse().getAggregateId())
                .count());
        if(numberOfExecutionsOfCourse == 1) {
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
    public void enrollStudent(Integer courseExecutionAggregateId, ExecutionStudent executionStudent, UnitOfWork unitOfWork) {
        CourseExecution oldCourseExecution = getCausalCourseExecutionLocal(courseExecutionAggregateId, unitOfWork);

        if(!executionStudent.isActive()){
            throw new TutorException(ErrorMessage.INACTIVE_USER, executionStudent.getAggregateId());
        }

        CourseExecution newCourseExecution = new CourseExecution(oldCourseExecution);
        newCourseExecution.addStudent(executionStudent);
        unitOfWork.registerChanged(newCourseExecution);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Set<CourseExecutionDto> getCourseExecutionsByUser(Integer userAggregateId, UnitOfWork unitOfWork) {
        return courseExecutionRepository.findAll().stream()
                .map(CourseExecution::getAggregateId)
                .map(aggregateId -> getCausalCourseExecutionLocal(aggregateId, unitOfWork))
                .filter(ce -> ce.hasStudent(userAggregateId))
                .map(ce -> new CourseExecutionDto(ce))
                .collect(Collectors.toSet());

    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeStudentFromCourseExecution(Integer courseExecutionAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        CourseExecution oldCourseExecution = getCausalCourseExecutionLocal(courseExecutionAggregateId, unitOfWork);
        CourseExecution newCourseExecution = new CourseExecution(oldCourseExecution);
        newCourseExecution.removeStudent(userAggregateId);
        unitOfWork.registerChanged(newCourseExecution);
        unitOfWork.addEvent(new UnerollStudentFromCourseExecutionEvent(courseExecutionAggregateId, userAggregateId));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserDto getStudentByExecutionIdAndUserId(Integer executionAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        CourseExecution courseExecution = getCausalCourseExecutionLocal(executionAggregateId, unitOfWork);
        if(!courseExecution.hasStudent(userAggregateId)) {
            throw new TutorException(COURSE_EXECUTION_STUDENT_NOT_FOUND, userAggregateId, executionAggregateId);
        }
        return courseExecution.findStudent(userAggregateId).buildDto();
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void anonymizeStudent(Integer executionAggregateId, Integer userAggregateId, UnitOfWork unitOfWork) {
        CourseExecution oldExecution = getCausalCourseExecutionLocal(executionAggregateId, unitOfWork);
        if(!oldExecution.hasStudent(userAggregateId)) {
            throw new TutorException(COURSE_EXECUTION_STUDENT_NOT_FOUND, userAggregateId, executionAggregateId);
        }
        CourseExecution newExecution = new CourseExecution(oldExecution);
        newExecution.findStudent(userAggregateId).anonymize();
        unitOfWork.registerChanged(newExecution);
        unitOfWork.addEvent(new AnonymizeExecutionStudentEvent(executionAggregateId, "ANONYMOUS", "ANONYMOUS", userAggregateId));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateExecutionStudentName(Integer executionAggregateId, Integer userAggregateId, String name, UnitOfWork unitOfWork) {
        CourseExecution oldExecution = getCausalCourseExecutionLocal(executionAggregateId, unitOfWork);
        if(!oldExecution.hasStudent(userAggregateId)) {
            throw new TutorException(COURSE_EXECUTION_STUDENT_NOT_FOUND, userAggregateId, executionAggregateId);
        }
        CourseExecution newExecution = new CourseExecution(oldExecution);
        newExecution.findStudent(userAggregateId).setName(name);
        unitOfWork.registerChanged(newExecution);
        unitOfWork.addEvent(new UpdateExecutionStudentNameEvent(executionAggregateId, userAggregateId, name));
    }


    /************************************************ EVENT PROCESSING ************************************************/


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseExecution removeUser(Integer executionAggregateId, Integer userAggregateId, Integer aggregateEventVersion, UnitOfWork unitOfWork) {
        CourseExecution oldExecution = getCausalCourseExecutionLocal(executionAggregateId, unitOfWork);
        CourseExecution newExecution = new CourseExecution(oldExecution);
        newExecution.findStudent(userAggregateId).setState(INACTIVE);
        unitOfWork.registerChanged(newExecution);
        unitOfWork.addEvent(new UnerollStudentFromCourseExecutionEvent(executionAggregateId, userAggregateId));
        return newExecution;
    }
}
