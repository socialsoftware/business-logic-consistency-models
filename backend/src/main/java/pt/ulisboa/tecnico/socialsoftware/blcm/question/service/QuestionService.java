package pt.ulisboa.tecnico.socialsoftware.blcm.question.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.UpdateQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork.UnitOfWork;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.QuestionCourse;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.domain.QuestionTopic;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.repository.QuestionRepository;

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
    private EventRepository eventRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuestionDto getCausalQuestionRemote(Integer aggregateId, UnitOfWork unitOfWork) {
        return new QuestionDto(getCausalQuestionLocal(aggregateId, unitOfWork));
    }


    public Question getCausalQuestionLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        Question question = questionRepository.findCausal(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(ErrorMessage.QUESTION_NOT_FOUND, aggregateId));

        if(question.getState() == Aggregate.AggregateState.DELETED) {
            throw new TutorException(ErrorMessage.QUESTION_DELETED, question.getAggregateId());
        }

        List<Event> allEvents = eventRepository.findAll();
        unitOfWork.addToCausalSnapshot(question, allEvents);
        return question;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<QuestionDto> findQuestionsByCourseAggregateId(Integer courseAggregateId, UnitOfWork unitOfWork) {
        return questionRepository.findAll().stream()
                .filter(q -> q.getCourse().getAggregateId() == courseAggregateId)
                .map(Question::getAggregateId)
                .distinct()
                .map(id -> getCausalQuestionLocal(id, unitOfWork))
                .map(QuestionDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuestionDto createQuestion(QuestionCourse course, QuestionDto questionDto, List<QuestionTopic> questionTopics, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();

        Question question = new Question(aggregateId, course, questionDto, questionTopics);
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
        Question oldQuestion = getCausalQuestionLocal(questionDto.getAggregateId(), unitOfWork);
        Question newQuestion = new Question(oldQuestion);
        newQuestion.update(questionDto);
        unitOfWork.registerChanged(newQuestion);
        unitOfWork.addEvent(new UpdateQuestionEvent(newQuestion.getAggregateId(), newQuestion.getTitle(), newQuestion.getContent()));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeQuestion(Integer courseAggregateId, UnitOfWork unitOfWork) {
        Question oldQuestion = getCausalQuestionLocal(courseAggregateId, unitOfWork);
        Question newQuestion = new Question(oldQuestion);
        newQuestion.remove();
        unitOfWork.registerChanged(newQuestion);
        unitOfWork.addEvent(new RemoveQuestionEvent(newQuestion));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateQuestionTopics(Integer courseAggregateId, Set<QuestionTopic> topics, UnitOfWork unitOfWork) {
        Question oldQuestion = getCausalQuestionLocal(courseAggregateId, unitOfWork);
        Question newQuestion = new Question(oldQuestion);
        newQuestion.setTopics(topics);
        unitOfWork.registerChanged(newQuestion);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<QuestionDto> findQuestionsByTopics(List<Integer> topicIds, UnitOfWork unitOfWork) {
        Set<Integer> questionAggregateIds = questionRepository.findAll().stream()
                .filter(q -> {
                    for(QuestionTopic qt : q.getTopics()) {
                        if (topicIds.contains(qt.getAggregateId())) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(Question::getAggregateId)
                .collect(Collectors.toSet());
        return questionAggregateIds.stream()
                .map(id -> getCausalQuestionLocal(id, unitOfWork))
                .map(QuestionDto::new)
                .collect(Collectors.toList());

    }

    /************************************************ EVENT PROCESSING ************************************************/

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Question updateTopic(Integer questionAggregateId, Integer topicAggregateId, String topicName, Integer aggregateVersion, UnitOfWork unitOfWork) {
        Question oldQuestion = getCausalQuestionLocal(questionAggregateId, unitOfWork);
        Question newQuestion = new Question(oldQuestion);

        QuestionTopic questionTopic = newQuestion.findTopic(topicAggregateId);
        /*
        if(questionTopic != null && questionTopic.getAggregateId().equals(topicAggregateId) && questionTopic.getVersion() >= aggregateVersion) {
            return null;
        }
        */
        questionTopic.setName(topicName);
        questionTopic.setVersion(aggregateVersion);
        unitOfWork.registerChanged(newQuestion);
        return newQuestion;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Question removeTopic(Integer questionAggregateId, Integer topicAggregateId, Integer aggregateVersion, UnitOfWork unitOfWork) {
        Question oldQuestion = getCausalQuestionLocal(questionAggregateId, unitOfWork);
        Question newQuestion = new Question(oldQuestion);

        QuestionTopic questionTopic = newQuestion.findTopic(topicAggregateId);
        if(questionTopic != null && questionTopic.getAggregateId().equals(topicAggregateId) && questionTopic.getVersion() >= aggregateVersion) {
            return null;
        }

        questionTopic.setState(Aggregate.AggregateState.INACTIVE);
        unitOfWork.registerChanged(newQuestion);
        return newQuestion;
    }


}
