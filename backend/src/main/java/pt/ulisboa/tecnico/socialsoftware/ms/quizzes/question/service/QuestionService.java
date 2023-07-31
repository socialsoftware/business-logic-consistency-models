package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.tcc.QuestionTCC;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.event.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.event.publish.UpdateQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.QuestionCourse;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.QuestionTopic;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.dto.TopicDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;
    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuestionDto getQuestionById(Integer aggregateId, UnitOfWork unitOfWork) {
        return new QuestionDto((Question) unitOfWorkService.aggregateLoadAndRegisterRead(aggregateId, unitOfWork));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<QuestionDto> findQuestionsByCourseAggregateId(Integer courseAggregateId, UnitOfWork unitOfWork) {
        return questionRepository.findAll().stream()
                .filter(q -> q.getQuestionCourse().getCourseAggregateId() == courseAggregateId)
                .map(Question::getAggregateId)
                .distinct()
                .map(id -> (Question) unitOfWorkService.aggregateLoadAndRegisterRead(id, unitOfWork))
                .map(QuestionDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuestionDto createQuestion(QuestionCourse course, QuestionDto questionDto, List<TopicDto> topics, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();

        List<QuestionTopic> questionTopics = topics.stream()
                .map(QuestionTopic::new)
                .collect(Collectors.toList());

        Question question = new QuestionTCC(aggregateId, course, questionDto, questionTopics);
        unitOfWork.registerChanged(question);
        return new QuestionDto(question);
    }

    private void checkInput(QuestionCourse course, QuestionDto questionDto) {
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateQuestion(QuestionDto questionDto, UnitOfWork unitOfWork) {
        Question oldQuestion = (Question) unitOfWorkService.aggregateLoadAndRegisterRead(questionDto.getAggregateId(), unitOfWork);
        Question newQuestion = new QuestionTCC((QuestionTCC) oldQuestion);
        newQuestion.update(questionDto);
        unitOfWork.registerChanged(newQuestion);
        unitOfWork.addEvent(new UpdateQuestionEvent(newQuestion.getAggregateId(), newQuestion.getTitle(), newQuestion.getContent()));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeQuestion(Integer courseAggregateId, UnitOfWork unitOfWork) {
        Question oldQuestion = (Question) unitOfWorkService.aggregateLoadAndRegisterRead(courseAggregateId, unitOfWork);
        Question newQuestion = new QuestionTCC((QuestionTCC) oldQuestion);
        newQuestion.remove();
        unitOfWork.registerChanged(newQuestion);
        unitOfWork.addEvent(new RemoveQuestionEvent(newQuestion.getAggregateId()));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateQuestionTopics(Integer courseAggregateId, Set<QuestionTopic> topics, UnitOfWork unitOfWork) {
        Question oldQuestion = (Question) unitOfWorkService.aggregateLoadAndRegisterRead(courseAggregateId, unitOfWork);
        Question newQuestion = new QuestionTCC((QuestionTCC) oldQuestion);
        newQuestion.setQuestionTopics(topics);
        unitOfWork.registerChanged(newQuestion);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<QuestionDto> findQuestionsByTopicIds(List<Integer> topicIds, UnitOfWork unitOfWork) {
        Set<Integer> questionAggregateIds = questionRepository.findAll().stream()
                .filter(q -> {
                    for (QuestionTopic qt : q.getQuestionTopics()) {
                        if (topicIds.contains(qt.getTopicAggregateId())) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(Question::getAggregateId)
                .collect(Collectors.toSet());
        return questionAggregateIds.stream()
                .map(id -> (Question) unitOfWorkService.aggregateLoadAndRegisterRead(id, unitOfWork))
                .map(QuestionDto::new)
                .collect(Collectors.toList());

    }

    /************************************************ EVENT PROCESSING ************************************************/

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Question updateTopic(Integer questionAggregateId, Integer topicAggregateId, String topicName, Integer aggregateVersion, UnitOfWork unitOfWork) {
        Question oldQuestion = (Question) unitOfWorkService.aggregateLoadAndRegisterRead(questionAggregateId, unitOfWork);
        Question newQuestion = new QuestionTCC((QuestionTCC) oldQuestion);

        QuestionTopic questionTopic = newQuestion.findTopic(topicAggregateId);
        /*
        if(questionTopic != null && questionTopic.getAggregateId().equals(topicAggregateId) && questionTopic.getVersion() >= aggregateVersion) {
            return null;
        }
        */
        questionTopic.setTopicName(topicName);
        questionTopic.setTopicVersion(aggregateVersion);
        unitOfWork.registerChanged(newQuestion);
        return newQuestion;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Question removeTopic(Integer questionAggregateId, Integer topicAggregateId, Integer aggregateVersion, UnitOfWork unitOfWork) {
        Question oldQuestion = (Question) unitOfWorkService.aggregateLoadAndRegisterRead(questionAggregateId, unitOfWork);
        Question newQuestion = new QuestionTCC((QuestionTCC) oldQuestion);

        QuestionTopic questionTopic = newQuestion.findTopic(topicAggregateId);
        if(questionTopic != null && questionTopic.getTopicAggregateId().equals(topicAggregateId) && questionTopic.getTopicVersion() >= aggregateVersion) {
            return null;
        }

        questionTopic.setState(Aggregate.AggregateState.INACTIVE);
        unitOfWork.registerChanged(newQuestion);
        return newQuestion;
    }


}
