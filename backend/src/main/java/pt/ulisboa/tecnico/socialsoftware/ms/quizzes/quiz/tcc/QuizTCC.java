package pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.tcc;

import jakarta.persistence.Entity;
import pt.ulisboa.tecnico.socialsoftware.ms.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.CausalConsistency;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.QuizCourseExecution;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.domain.QuizType;
import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.quiz.dto.QuizDto;

import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class QuizTCC extends Quiz implements CausalConsistency {
    public QuizTCC() {
    }

    public QuizTCC(Integer aggregateId, QuizCourseExecution quizCourseExecution, Set<QuizQuestion> quizQuestions, QuizDto quizDto, QuizType quizType) {
        super(aggregateId, quizCourseExecution, quizQuestions, quizDto, quizType);
    }

    public QuizTCC(QuizTCC other) {
        super(other);
    }

    public Set<String> getFieldsChangedByFunctionalities() {
        // we dont add the courseExecution because it can only change through events and the only events that comes from it is the delete which deletes the quiz
        return Set.of("availableDate", "conclusionDate", "resultsDate", "title" ,"quizQuestions");
    }

    public Set<String[]> getIntentions() {
        return Set.of(
                new String[]{"availableDate", "conclusionDate"},
                new String[]{"availableDate", "resultsDate"},
                new String[]{"conclusionDate", "resultsDate"});
    }

    public Aggregate mergeFields(Set<String> toCommitVersionChangedFields, Aggregate committedVersion, Set<String> committedVersionChangedFields) {
        QuizTCC committedQuiz = (QuizTCC) committedVersion;
        QuizTCC mergedQuiz = new QuizTCC(this);

        mergeAvailableDate(toCommitVersionChangedFields, committedQuiz, mergedQuiz);
        mergeConclusionDate(toCommitVersionChangedFields, committedQuiz, mergedQuiz);
        mergeResultsDate(toCommitVersionChangedFields, committedQuiz, mergedQuiz);
        mergeTitle(toCommitVersionChangedFields, committedQuiz, mergedQuiz);
        mergeQuizQuestions(toCommitVersionChangedFields, committedQuiz, mergedQuiz);
        return mergedQuiz;
    }

    private void mergeAvailableDate(Set<String> toCommitVersionChangedFields, Quiz committedQuiz, Quiz mergedQuiz) {
        if (toCommitVersionChangedFields.contains("availableDate")) {
            mergedQuiz.setAvailableDate(getAvailableDate());
        } else {
            mergedQuiz.setAvailableDate(committedQuiz.getAvailableDate());
        }
    }

    private void mergeConclusionDate(Set<String> toCommitVersionChangedFields, Quiz committedQuiz, Quiz mergedQuiz) {
        if(toCommitVersionChangedFields.contains("conclusionDate")) {
            mergedQuiz.setConclusionDate(getConclusionDate());
        } else {
            mergedQuiz.setConclusionDate(committedQuiz.getConclusionDate());
        }
    }

    private void mergeResultsDate(Set<String> toCommitVersionChangedFields, Quiz committedQuiz, Quiz mergedQuiz) {
        if(toCommitVersionChangedFields.contains("resultsDate")) {
            mergedQuiz.setResultsDate(getResultsDate());
        } else {
            mergedQuiz.setResultsDate(committedQuiz.getResultsDate());
        }
    }

    private void mergeTitle(Set<String> toCommitVersionChangedFields, Quiz committedQuiz, Quiz mergedQuiz) {
        if(toCommitVersionChangedFields.contains("title")) {
            mergedQuiz.setTitle(getTitle());
        } else {
            mergedQuiz.setTitle(committedQuiz.getTitle());
        }
    }

    private void mergeQuizQuestions(Set<String> toCommitVersionChangedFields, Quiz committedQuiz, Quiz mergedQuiz) {
        if(toCommitVersionChangedFields.contains("quizQuestions")) {
            mergedQuiz.setQuizQuestions(getQuizQuestions().stream().map(QuizQuestion::new).collect(Collectors.toSet()));
        } else {
            mergedQuiz.setQuizQuestions(committedQuiz.getQuizQuestions().stream().map(QuizQuestion::new).collect(Collectors.toSet()));
        }
    }
}
