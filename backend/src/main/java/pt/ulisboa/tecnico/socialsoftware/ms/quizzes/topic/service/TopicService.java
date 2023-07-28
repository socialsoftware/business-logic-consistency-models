package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.service.CausalConsistencyService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.tcc.TopicTCC;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.tcc.TopicTCCRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.event.publish.DeleteTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventRepository;
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
    private CausalConsistencyService causalConsistencyService;

    @Autowired
    private TopicTCCRepository topicTCCRepository;

    @Autowired
    private EventRepository eventRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TopicDto AddTopicCausalSnapshot(Integer topicAggregateId, UnitOfWork unitOfWork) {
        return new TopicDto((TopicTCC) causalConsistencyService.addAggregateCausalSnapshot(topicAggregateId, unitOfWork));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TopicDto createTopic(TopicDto topicDto, TopicCourse course, UnitOfWork unitOfWorkWorkService) {
        TopicTCC topic = new TopicTCC(aggregateIdGeneratorService.getNewAggregateId(),
                topicDto.getName(), course);
        unitOfWorkWorkService.registerChanged(topic);
        return new TopicDto(topic);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<TopicDto> findCourseByTopicId(Integer courseAggregateId, UnitOfWork unitOfWork) {
        return topicTCCRepository.findAll().stream()
                .filter(t -> courseAggregateId == t.getTopicCourse().getCourseAggregateId())
                .map(Topic::getAggregateId)
                .distinct()
                .map(aggregateId -> (TopicTCC) causalConsistencyService.addAggregateCausalSnapshot(aggregateId, unitOfWork))
                .map(TopicDto::new)
                .collect(Collectors.toList());

    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateTopic(TopicDto topicDto, UnitOfWork unitOfWork) {
        TopicTCC oldTopic = (TopicTCC) causalConsistencyService.addAggregateCausalSnapshot(topicDto.getAggregateId(), unitOfWork);
        TopicTCC newTopic = new TopicTCC(oldTopic);
        newTopic.setName(topicDto.getName());
        unitOfWork.registerChanged(newTopic);
        unitOfWork.addEvent(new UpdateTopicEvent(newTopic.getAggregateId(), newTopic.getName()));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteTopic(Integer topicAggregateId, UnitOfWork unitOfWork) {
        TopicTCC oldTopic = (TopicTCC) causalConsistencyService.addAggregateCausalSnapshot(topicAggregateId, unitOfWork);
        TopicTCC newTopic = new TopicTCC(oldTopic);
        newTopic.remove();
        unitOfWork.registerChanged(newTopic);
        unitOfWork.addEvent(new DeleteTopicEvent(newTopic.getAggregateId()));
    }
}
