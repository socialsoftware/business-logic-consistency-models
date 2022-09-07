package pt.ulisboa.tecnico.socialsoftware.blcm.course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.service.CourseService;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWorkService;

@Service
public class CourseFunctionalities {
    @Autowired
    private CourseService courseService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;
/*
    public CourseDto createCourse(CourseDto courseDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        CourseDto courseDto1 = courseService.getAndOrCreateCourseRemote(courseDto, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
        return courseDto1;
    }

 */
}
