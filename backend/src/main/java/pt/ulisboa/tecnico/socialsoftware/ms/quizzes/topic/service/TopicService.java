package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.tcc.TopicTCC;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.event.publish.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.domain.TopicCourse;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopicService {

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    @Autowired
    private TopicRepository topicRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TopicDto getTopicById(Integer topicAggregateId, UnitOfWork unitOfWork) {
        return new TopicDto((Topic) unitOfWorkService.aggregateLoadAndRegisterRead(topicAggregateId, unitOfWork));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TopicDto createTopic(TopicDto topicDto, TopicCourse course, UnitOfWork unitOfWorkWorkService) {
        Topic topic = new TopicTCC(aggregateIdGeneratorService.getNewAggregateId(),
                topicDto.getName(), course);
        unitOfWorkWorkService.registerChanged(topic);
        return new TopicDto(topic);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TopicDto> findTopicsByCourseId(Integer courseAggregateId, UnitOfWork unitOfWork) {
        return topicRepository.findAll().stream()
                .filter(t -> courseAggregateId == t.getTopicCourse().getCourseAggregateId())
                .map(Topic::getAggregateId)
                .distinct()
                .map(aggregateId -> (Topic) unitOfWorkService.aggregateLoadAndRegisterRead(aggregateId, unitOfWork))
                .map(TopicDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateTopic(TopicDto topicDto, UnitOfWork unitOfWork) {
        Topic oldTopic = (Topic) unitOfWorkService.aggregateLoadAndRegisterRead(topicDto.getAggregateId(), unitOfWork);
        Topic newTopic = new TopicTCC((TopicTCC) oldTopic);
        newTopic.setName(topicDto.getName());
        unitOfWork.registerChanged(newTopic);
        unitOfWork.addEvent(new UpdateTopicEvent(newTopic.getAggregateId(), newTopic.getName()));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteTopic(Integer topicAggregateId, UnitOfWork unitOfWork) {
        Topic oldTopic = (Topic) unitOfWorkService.aggregateLoadAndRegisterRead(topicAggregateId, unitOfWork);
        Topic newTopic = new TopicTCC((TopicTCC) oldTopic);
        newTopic.remove();
        unitOfWork.registerChanged(newTopic);
        unitOfWork.addEvent(new DeleteTopicEvent(newTopic.getAggregateId()));
    }
}
