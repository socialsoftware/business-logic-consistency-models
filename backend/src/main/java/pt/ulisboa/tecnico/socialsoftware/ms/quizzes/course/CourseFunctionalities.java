package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.service.CourseService;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;

@Service
public class CourseFunctionalities {
    @Autowired
    private CourseService courseService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

}
