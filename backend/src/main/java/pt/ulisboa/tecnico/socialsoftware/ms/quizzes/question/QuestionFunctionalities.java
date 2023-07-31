package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.QuestionCourse;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.QuestionTopic;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.service.TopicService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.course.service.CourseService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;

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
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        return questionService.getQuestionById(aggregateId, unitOfWork);
    }

    public List<QuestionDto> findQuestionsByCourseAggregateId(Integer courseAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        return questionService.findQuestionsByCourseAggregateId(courseAggregateId, unitOfWork);
    }

    public QuestionDto createQuestion(Integer courseAggregateId, QuestionDto questionDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        QuestionCourse course = new QuestionCourse(courseService.getCourseById(courseAggregateId, unitOfWork));
        /*
            COURSE_SAME_TOPICS_COURSE
         */

        for (TopicDto topicDto : questionDto.getTopicDto()) {
            if (!topicDto.getCourseId().equals(courseAggregateId)) {
                throw new TutorException(ErrorMessage.QUESTION_TOPIC_INVALID_COURSE, topicDto.getAggregateId(), courseAggregateId);
            }
        }

        List<TopicDto> topics = questionDto.getTopicDto().stream()
                .map(topicDto -> topicService.getTopicById(topicDto.getAggregateId(), unitOfWork))
                .collect(Collectors.toList());

        QuestionDto questionDto1 = questionService.createQuestion(course, questionDto, topics, unitOfWork);

        unitOfWorkService.commit(unitOfWork);
        return questionDto1;
    }

    public void updateQuestion(QuestionDto questionDto) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        questionService.updateQuestion(questionDto, unitOfWork);

        unitOfWorkService.commit(unitOfWork);
    }

    public void removeQuestion(Integer questionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        questionService.removeQuestion(questionAggregateId, unitOfWork);

        unitOfWorkService.commit(unitOfWork);
    }


    public void updateQuestionTopics(Integer courseAggregateId, List<Integer> topicIds) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork(new Throwable().getStackTrace()[0].getMethodName());
        Set<QuestionTopic> topics = topicIds.stream()
                        .map(id -> topicService.getTopicById(id, unitOfWork))
                        .map(QuestionTopic::new)
                        .collect(Collectors.toSet());

        questionService.updateQuestionTopics(courseAggregateId, topics, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

}
