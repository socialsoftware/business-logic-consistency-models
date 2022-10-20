package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
public class AnswerQuiz {
    @Column(name = "quiz_aggregate_id")
    private Integer aggregateId;

    @Column(name = "quiz_version")
    private Integer version;

    @Embedded
    private AnswerQuizExecution courseExecution;

    @ElementCollection
    private List<Integer> quizQuestionsAggregateIds;

    public AnswerQuiz() {

    }

    public AnswerQuiz(QuizDto quizDto) {
        setAggregateId(quizDto.getAggregateId());
        setVersion(quizDto.getVersion());
        setQuizQuestionsAggregateIds(quizDto.getQuestionDtos().stream()
                .map(QuestionDto::getAggregateId)
                .collect(Collectors.toList()));
        setCourseExecution(new AnswerQuizExecution(quizDto.getCourseExecutionAggregateId(), quizDto.getCourseExecutionVersion()));
    }

    public AnswerQuiz(AnswerQuiz other) {
        setAggregateId(other.getAggregateId());
        setVersion(other.getVersion());
        setQuizQuestionsAggregateIds(new ArrayList<>(other.getQuizQuestionsAggregateIds()));
        setCourseExecution(new AnswerQuizExecution(other.getCourseExecution()));
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Integer aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<Integer> getQuizQuestionsAggregateIds() {
        return quizQuestionsAggregateIds;
    }

    public void setQuizQuestionsAggregateIds(List<Integer> quizQuestionsAggregateIds) {
        this.quizQuestionsAggregateIds = quizQuestionsAggregateIds;
    }

    public AnswerQuizExecution getCourseExecution() {
        return courseExecution;
    }

    public void setCourseExecution(AnswerQuizExecution courseExecution) {
        this.courseExecution = courseExecution;
    }
}
