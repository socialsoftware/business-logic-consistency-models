package pt.ulisboa.tecnico.socialsoftware.blcm.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.RemoveQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.UpdateQuestionEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;

import javax.transaction.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AggregateIdGeneratorService aggregateIdGeneratorService;

    @Transactional
    public QuestionDto getCausalQuestionRemote(Integer aggregateId, UnitOfWork unitOfWork) {
        return new QuestionDto(getCausalQuestionLocal(aggregateId, unitOfWork));
    }

    @Transactional
    public Question getCausalQuestionLocal(Integer aggregateId, UnitOfWork unitOfWork) {
        Question question = questionRepository.findByAggregateIdAndVersion(aggregateId, unitOfWork.getVersion())
                .orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, aggregateId));

        if(question.getState().equals(DELETED)) {
            throw new TutorException(ErrorMessage.QUESTION_DELETED, question.getAggregateId());
        }

        question.checkDependencies(unitOfWork);
        unitOfWork.addCurrentReadDependencies(question.getDependenciesMap());
        return question;
    }

    @Transactional
    public List<QuestionDto> findQuestionsByCourseAggregateId(Integer courseAggregateId, UnitOfWork unitOfWork) {
        return questionRepository.findAll().stream()
                .filter(q -> q.getCourse().getAggregateId() == courseAggregateId)
                .map(Question::getAggregateId)
                .distinct()
                .map(id -> getCausalQuestionLocal(id, unitOfWork))
                .map(QuestionDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestionDto createQuestion(QuestionCourse course, QuestionDto questionDto, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();

        Question question = new Question(aggregateId, course, questionDto);
        unitOfWork.addUpdatedObject(question);
        return new QuestionDto(question);
    }

    private void checkInput(QuestionCourse course, QuestionDto questionDto) {
        // TODO
    }

    @Transactional
    public void updateQuestion(QuestionDto questionDto, UnitOfWork unitOfWork) {
        Question oldQuestion = getCausalQuestionLocal(questionDto.getAggregateId(), unitOfWork);
        Question newQuestion = new Question(oldQuestion);
        newQuestion.update(questionDto);
        unitOfWork.addUpdatedObject(newQuestion);
        unitOfWork.addEvent(new UpdateQuestionEvent(newQuestion.getAggregateId(), newQuestion.getTitle(), newQuestion.getContent()));
    }

    @Transactional
    public void removeQuestion(Integer courseAggregateId, UnitOfWork unitOfWork) {
        Question oldQuestion = getCausalQuestionLocal(courseAggregateId, unitOfWork);
        Question newQuestion = new Question(oldQuestion);
        // TODO check remove conditions, maybe not needed because they are written based on info from downstream aggregates
        newQuestion.remove();
        unitOfWork.addUpdatedObject(newQuestion);
        unitOfWork.addEvent(new RemoveQuestionEvent(newQuestion.getAggregateId()));
    }

    @Transactional
    public void updateQuestionTopics(Integer courseAggregateId, Set<QuestionTopic> topics, UnitOfWork unitOfWork) {
        Question oldQuestion = getCausalQuestionLocal(courseAggregateId, unitOfWork);
        Question newQuestion = new Question(oldQuestion);
        newQuestion.setTopics(topics);
        unitOfWork.addUpdatedObject(newQuestion);
    }

    @Transactional
    public List<QuestionDto> findQuestionsByTopics(List<Integer> topicIds, UnitOfWork unitOfWork) {
        Set<Integer> questionAggregateIds = questionRepository.findAll().stream()
                .filter(q -> {
                    for(QuestionTopic qt : q.getTopics()) {
                        if (topicIds.contains(qt.getTopicAggregateId())) {
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
}
