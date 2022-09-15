package pt.ulisboa.tecnico.socialsoftware.blcm.course.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.domain.Course;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.dto.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.repository.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;

import javax.transaction.Transactional;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;

@Service
public class CourseService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
    public CourseDto getCausalCourseRemote(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        return new CourseDto(getCausalCourseLocal(aggregateId, unitOfWorkWorkService));
    }

    // intended for requests from local functionalities

    public Course getCausalCourseLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        Course course = courseRepository.findByAggregateIdAndVersion(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(ErrorMessage.COURSE_NOT_FOUND, aggregateId));

        if(course.getState().equals(DELETED)) {
            throw new TutorException(ErrorMessage.COURSE_DELETED, course.getAggregateId());
        }

        unitOfWork.addToCausalSnapshot(course);
        return course;
    }



    @Transactional
    public CourseExecutionDto getAndOrCreateCourseRemote(CourseExecutionDto courseExecutionDto, UnitOfWork unitOfWork) {
        Course course = getCausalCourseLocalByName(courseExecutionDto.getName(), unitOfWork);
        if(course == null) {
            Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
            course = new Course(aggregateId, unitOfWork.getVersion(), courseExecutionDto);
            unitOfWork.addUpdatedObject(course);
        }
        courseExecutionDto.setCourseAggregateId(course.getAggregateId());
        courseExecutionDto.setName(course.getName());
        courseExecutionDto.setType(course.getType().toString());
        courseExecutionDto.setCourseVersion(course.getVersion());
        return courseExecutionDto;
    }

    private Course getCausalCourseLocalByName(String courseName, UnitOfWork unitOfWork) {
        Course course = courseRepository.findByAggregateNameAndVersion(courseName, unitOfWork.getVersion())
                .orElse(null);
        if(course != null) {
            unitOfWork.addToCausalSnapshot(course);

        }
        return course;
    }


}
