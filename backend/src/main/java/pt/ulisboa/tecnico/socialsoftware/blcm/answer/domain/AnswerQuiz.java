package pt.ulisboa.tecnico.socialsoftware.blcm.answer.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.dto.QuizDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
public class AnswerQuiz {
    private Integer quizAggregateId;
    private Integer quizVersion;
    @ElementCollection
    private List<Integer> quizQuestionsAggregateIds;

    public AnswerQuiz() {
        this.quizAggregateId = 0;
    }

    public AnswerQuiz(QuizDto quizDto) {
        this.quizAggregateId = quizDto.getAggregateId();
        setQuizVersion(quizDto.getVersion());
        setQuizQuestionsAggregateIds(quizDto.getQuestionDtos().stream()
                .map(QuestionDto::getAggregateId)
                .collect(Collectors.toList()));
    }

    public AnswerQuiz(AnswerQuiz other) {
        this.quizAggregateId = other.getQuizAggregateId();
        setQuizVersion(other.getQuizVersion());
        setQuizQuestionsAggregateIds(new ArrayList<>(other.getQuizQuestionsAggregateIds()));
    }

    public Integer getQuizAggregateId() {
        return quizAggregateId;
    }

    public Integer getQuizVersion() {
        return quizVersion;
    }

    public void setQuizVersion(Integer quizVersion) {
        this.quizVersion = quizVersion;
    }

    public List<Integer> getQuizQuestionsAggregateIds() {
        return quizQuestionsAggregateIds;
    }

    public void setQuizQuestionsAggregateIds(List<Integer> quizQuestionsAggregateIds) {
        this.quizQuestionsAggregateIds = quizQuestionsAggregateIds;
    }
}
