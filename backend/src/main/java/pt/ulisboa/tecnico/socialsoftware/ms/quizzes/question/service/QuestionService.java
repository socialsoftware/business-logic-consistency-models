package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.repository.CausalConsistencyRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.service.CausalConsistencyService;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.tcc.QuestionTCC;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.event.publish.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.event.publish.UpdateQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.QuestionCourse;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.domain.QuestionTopic;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.question.tcc.QuestionTCCRepository;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.topic.dto.TopicDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionTCCRepository questionTCCRepository;

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Autowired
    private CausalConsistencyService causalConsistencyService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CausalConsistencyRepository causalConsistencyRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuestionDto addQuestionCausalSnapshot(Integer aggregateId, UnitOfWork unitOfWork) {
        return new QuestionDto((QuestionTCC) causalConsistencyService.addAggregateCausalSnapshot(aggregateId, unitOfWork));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<QuestionDto> findQuestionsByCourseAggregateId(Integer courseAggregateId, UnitOfWork unitOfWork) {
        return questionTCCRepository.findAll().stream()
                .filter(q -> q.getQuestionCourse().getCourseAggregateId() == courseAggregateId)
                .map(Question::getAggregateId)
                .distinct()
                .map(id -> (QuestionTCC) causalConsistencyService.addAggregateCausalSnapshot(id, unitOfWork))
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

        QuestionTCC question = new QuestionTCC(aggregateId, course, questionDto, questionTopics);
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
        QuestionTCC oldQuestion = (QuestionTCC) causalConsistencyService.addAggregateCausalSnapshot(questionDto.getAggregateId(), unitOfWork);
        QuestionTCC newQuestion = new QuestionTCC(oldQuestion);
        newQuestion.update(questionDto);
        unitOfWork.registerChanged(newQuestion);
        unitOfWork.addEvent(new UpdateQuestionEvent(newQuestion.getAggregateId(), newQuestion.getTitle(), newQuestion.getContent()));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeQuestion(Integer courseAggregateId, UnitOfWork unitOfWork) {
        QuestionTCC oldQuestion = (QuestionTCC) causalConsistencyService.addAggregateCausalSnapshot(courseAggregateId, unitOfWork);
        QuestionTCC newQuestion = new QuestionTCC(oldQuestion);
        newQuestion.remove();
        unitOfWork.registerChanged(newQuestion);
        unitOfWork.addEvent(new RemoveQuestionEvent(newQuestion.getAggregateId()));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateQuestionTopics(Integer courseAggregateId, Set<QuestionTopic> topics, UnitOfWork unitOfWork) {
        QuestionTCC oldQuestion = (QuestionTCC) causalConsistencyService.addAggregateCausalSnapshot(courseAggregateId, unitOfWork);
        QuestionTCC newQuestion = new QuestionTCC(oldQuestion);
        newQuestion.setQuestionTopics(topics);
        unitOfWork.registerChanged(newQuestion);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<QuestionDto> findQuestionsByTopics(List<Integer> topicIds, UnitOfWork unitOfWork) {
        Set<Integer> questionAggregateIds = questionTCCRepository.findAll().stream()
                .filter(q -> {
                    for(QuestionTopic qt : q.getQuestionTopics()) {
                        if (topicIds.contains(qt.getTopicAggregateId())) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(Question::getAggregateId)
                .collect(Collectors.toSet());
        return questionAggregateIds.stream()
                .map(id -> (QuestionTCC) causalConsistencyService.addAggregateCausalSnapshot(id, unitOfWork))
                .map(QuestionDto::new)
                .collect(Collectors.toList());

    }

    /************************************************ EVENT PROCESSING ************************************************/

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Question updateTopic(Integer questionAggregateId, Integer topicAggregateId, String topicName, Integer aggregateVersion, UnitOfWork unitOfWork) {
        QuestionTCC oldQuestion = (QuestionTCC) causalConsistencyService.addAggregateCausalSnapshot(questionAggregateId, unitOfWork);
        QuestionTCC newQuestion = new QuestionTCC(oldQuestion);

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
        QuestionTCC oldQuestion = (QuestionTCC) causalConsistencyService.addAggregateCausalSnapshot(questionAggregateId, unitOfWork);
        QuestionTCC newQuestion = new QuestionTCC(oldQuestion);

        QuestionTopic questionTopic = newQuestion.findTopic(topicAggregateId);
        if(questionTopic != null && questionTopic.getTopicAggregateId().equals(topicAggregateId) && questionTopic.getTopicVersion() >= aggregateVersion) {
            return null;
        }

        questionTopic.setState(Aggregate.AggregateState.INACTIVE);
        unitOfWork.registerChanged(newQuestion);
        return newQuestion;
    }


}
