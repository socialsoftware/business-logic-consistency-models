package pt.ulisboa.tecnico.socialsoftware.blcm.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.QuestionCourse;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.QuestionTopic;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.service.TopicService;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.event.publish.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.service.CourseService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWorkService;

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

    public List<QuestionDto> findQuestionsByCourseAggregateId(Integer courseAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return questionService.findQuestionsByCourseAggregateId(courseAggregateId, unitOfWork);
    }

    public QuestionDto createQuestion(Integer courseAggregateId, QuestionDto questionDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        QuestionCourse course = new QuestionCourse(courseService.getCausalCourseRemote(courseAggregateId, unitOfWork));
        /*
            COURSE_SAME_TOPICS_COURSE
         */

        for(TopicDto topicDto : questionDto.getTopicDto()) {
            if(!topicDto.getCourseId().equals(courseAggregateId)) {
                throw new TutorException(ErrorMessage.QUESTION_TOPIC_INVALID_COURSE, topicDto.getAggregateId(), courseAggregateId);
            }
        }

        List<QuestionTopic> questionTopics = questionDto.getTopicDto().stream()
                .map(topicDto -> topicService.getCausalTopicRemote(topicDto.getAggregateId(), unitOfWork))
                .map(QuestionTopic::new)
                .collect(Collectors.toList());

        QuestionDto questionDto1 = questionService.createQuestion(course, questionDto, questionTopics, unitOfWork);

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
