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
    public Question getCausalQuestionLocal(Integer aggregateId, UnitOfWork unitOfWorkWorkService) {
        Question question = questionRepository.findByAggregateIdAndVersion(aggregateId, unitOfWorkWorkService.getVersion())
                .orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, aggregateId));

        if(question.getState().equals(DELETED)) {
            throw new TutorException(ErrorMessage.QUESTION_DELETED, question.getAggregateId());
        }

        question.checkDependencies(unitOfWorkWorkService);
        return question;
    }

    public List<QuestionDto> findQuestionsByCourseAggregateId(Integer courseAggregateId, UnitOfWork unitOfWork) {
        return questionRepository.findAll().stream()
                .filter(q -> q.getCourse().getCourseAggregateId() == courseAggregateId)
                .map(Question::getAggregateId)
                .distinct()
                .map(id -> getCausalQuestionLocal(id, unitOfWork))
                .map(QuestionDto::new)
                .collect(Collectors.toList());
    }

    public QuestionDto createQuestion(QuestionCourse course, QuestionDto questionDto, UnitOfWork unitOfWork) {
        Integer aggregateId = aggregateIdGeneratorService.getNewAggregateId();
        Question question = new Question(aggregateId, unitOfWork.getVersion(), course, questionDto);
        questionRepository.save(question);
        unitOfWork.addUpdatedObject(question, "Question");
        return new QuestionDto(question);
    }

    private void checkInput(QuestionCourse course, QuestionDto questionDto) {
        // TODO
    }

    public void updateQuestion(QuestionDto questionDto, UnitOfWork unitOfWork) {
        Question oldQuestion = getCausalQuestionLocal(questionDto.getAggregateId(), unitOfWork);
        Question newQuestion = new Question(oldQuestion);
        newQuestion.update(questionDto);
        unitOfWork.addUpdatedObject(newQuestion, "Question");
        unitOfWork.addEvent(new UpdateQuestionEvent(newQuestion.getAggregateId(), newQuestion.getTitle(), newQuestion.getContent()));
    }

    public void removeQuestion(Integer courseAggregateId, UnitOfWork unitOfWork) {
        Question oldQuestion = getCausalQuestionLocal(courseAggregateId, unitOfWork);
        Question newQuestion = new Question(oldQuestion);
        // TODO check remove conditions, maybe not needed because they are written based on info from downstream aggregates
        newQuestion.remove();
        unitOfWork.addUpdatedObject(newQuestion, "Question");
        unitOfWork.addEvent(new RemoveQuestionEvent(newQuestion.getAggregateId()));
    }

    public void updateQuestionTopics(Integer courseAggregateId, Set<QuestionTopic> topics, UnitOfWork unitOfWork) {
        Question oldQuestion = getCausalQuestionLocal(courseAggregateId, unitOfWork);
        Question newQuestion = new Question(oldQuestion);
        newQuestion.setTopics(topics);
        unitOfWork.addUpdatedObject(newQuestion, "Question");
    }
}
