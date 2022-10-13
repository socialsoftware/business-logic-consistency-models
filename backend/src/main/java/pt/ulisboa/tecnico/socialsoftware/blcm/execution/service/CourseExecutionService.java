package pt.ulisboa.tecnico.socialsoftware.blcm.execution.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEvents;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.ProcessedEventsRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.ExecutionCourse;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.service.VersionService;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;
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

        unitOfWork.addToCausalSnapshot(execution);
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
}
