package pt.ulisboa.tecnico.socialsoftware.blcm.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.service.CourseService;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.service.TopicService;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWorkService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionFunctionalities {

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private TopicService topicService;

    public QuestionDto findQuestionByAggregateId(Integer aggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return questionService.getCausalQuestionRemote(aggregateId, unitOfWork);
    }

    public void getStatementQuestionDto(Integer aggregateId) {

    }

    public List<QuestionDto> findQuestionsByCourseAggregateId(Integer courseAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return questionService.findQuestionsByCourseAggregateId(courseAggregateId, unitOfWork);
    }

    public QuestionDto createQuestion(Integer questionAggregateId, QuestionDto questionDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        QuestionCourse course = new QuestionCourse(courseService.getCausalCourseRemote(questionAggregateId, unitOfWork));

        QuestionDto questionDto1 = questionService.createQuestion(course, questionDto, unitOfWork);

        unitOfWorkService.commit(unitOfWork);
        return questionDto1;
    }

    public void updateQuestion(QuestionDto questionDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        questionService.updateQuestion(questionDto, unitOfWork);

        unitOfWorkService.commit(unitOfWork);
    }

    public void removeQuestion(Integer questionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        questionService.removeQuestion(questionAggregateId, unitOfWork);

        unitOfWorkService.commit(unitOfWork);
    }


    public void updateQuestionTopics(Integer courseAggregateId, List<Integer> topicIds) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        Set<QuestionTopic> topics = topicIds.stream()
                        .map(id -> topicService.getCausalTopicRemote(id, unitOfWork))
                        .map(QuestionTopic::new)
                        .collect(Collectors.toSet());

        questionService.updateQuestionTopics(courseAggregateId, topics, unitOfWork);

        unitOfWorkService.commit(unitOfWork);
    }


}
