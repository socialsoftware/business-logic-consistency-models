package pt.ulisboa.tecnico.socialsoftware.blcm.execution.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.RemoveCourseExecutionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.ExecutionCourse;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.utils.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.blcm.version.service.VersionService;

import javax.transaction.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.*;
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

    @Transactional
    public CourseExecutionDto getCausalCourseExecutionRemote(Integer executionAggregateId, UnitOfWork unitOfWorkWorkService) {
        return new CourseExecutionDto(getCausalCourseExecutionLocal(executionAggregateId, unitOfWorkWorkService));
    }



    // intended for requests from local functionalities
    @Transactional
    public CourseExecution getCausalCourseExecutionLocal(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        CourseExecution execution = courseExecutionRepository.findByAggregateIdAndVersion(aggregateId, unitOfWorkWorkService.getVersion())
                .orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, aggregateId));

        if(execution.getState().equals(DELETED)) {
            throw new TutorException(COURSE_EXECUTION_DELETED, execution.getAggregateId());
        }

        execution.checkDependencies(unitOfWorkWorkService);
        return execution;
    }

    @Transactional
    public CourseExecutionDto createCourseExecution(CourseExecutionDto courseExecutionDto, ExecutionCourse executionCourse, UnitOfWork unitOfWork) {
        CourseExecution courseExecution = new CourseExecution(aggregateIdGeneratorService.getNewAggregateId(),
                unitOfWork.getVersion(), courseExecutionDto.getAcronym(),
                courseExecutionDto.getAcademicTerm(), DateHandler.toLocalDateTime(courseExecutionDto.getEndDate()), executionCourse);
        //courseExecutionRepository.save(courseExecution);

        unitOfWork.addUpdatedObject(courseExecution);
        // TODO replace with UoW commit after fixing it
        /*courseExecution.setState(ACTIVE);
        courseExecution.setVersion(versionService.getVersionNumber());
        versionService.incrementVersionNumber();
        courseExecutionRepository.save(courseExecution);*/
        return new CourseExecutionDto(courseExecution);
    }

    @Transactional
    public List<CourseExecutionDto> getAllCausalCourseExecutions(UnitOfWork unitOfWork) {
        Set<Integer> executionsAggregateIds = courseExecutionRepository.findAll().stream()
                .map(CourseExecution::getAggregateId)
                .collect(Collectors.toSet());
        return executionsAggregateIds.stream()
                .map(id -> getCausalCourseExecutionLocal(id, unitOfWork))
                .filter(ce -> !ce.getState().equals(DELETED) && !ce.getState().equals(INACTIVE))
                .map(CourseExecutionDto::new)
                .collect(Collectors.toList());
    }

    public void removeCourseExecution(Integer executionAggregateId, UnitOfWork unitOfWork) {

        CourseExecution oldCourseExecution = getCausalCourseExecutionLocal(executionAggregateId, unitOfWork);
        CourseExecution newCourseExecution = new CourseExecution(oldCourseExecution);

        Integer numberOfExecutionsOfCourse = Math.toIntExact(getAllCausalCourseExecutions(unitOfWork).stream()
                .filter(ce -> ce.getCourseAggregateId() == newCourseExecution.getCourse().getAggregateId())
                .count());
        if(numberOfExecutionsOfCourse == 1) {
            throw new TutorException(CANNOT_DELETE_COURSE_EXECUTION);
        }

        newCourseExecution.setState(DELETED);
        courseExecutionRepository.save(newCourseExecution);

        unitOfWork.addUpdatedObject(newCourseExecution);
        unitOfWork.addEvent(new RemoveCourseExecutionEvent(newCourseExecution.getAggregateId()));

    }
}
