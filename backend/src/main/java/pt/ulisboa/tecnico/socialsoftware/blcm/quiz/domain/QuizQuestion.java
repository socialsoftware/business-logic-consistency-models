package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipant;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipantAnswer;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Embeddable
public class QuizQuestion {
    @Column(name = "question_aggregate_id")
    private Integer aggregateId;

    @Column(name = "question_version")
    private Integer version;

    private String title;

    private String content;

    private Integer sequence;

    private Aggregate.AggregateState state;


    public QuizQuestion() {

    }


    public QuizQuestion(QuestionDto questionDto) {
        setAggregateId(questionDto.getAggregateId());
        setVersion(questionDto.getVersion());
        setTitle(questionDto.getTitle());
        setContent(questionDto.getContent());
        setSequence(questionDto.getSequence());
        setState(Aggregate.AggregateState.ACTIVE);
    }

    public QuizQuestion(QuizQuestion other) {
        setAggregateId(other.getAggregateId());
        setVersion(other.getVersion());
        setTitle(other.getTitle());
        setContent(other.getContent());
        setSequence(other.getSequence());
        setState(other.getState());
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Aggregate.AggregateState getState() {
        return state;
    }

    public void setState(Aggregate.AggregateState state) {
        this.state = state;
    }

    public QuestionDto buildDto() {
        QuestionDto questionDto = new QuestionDto();
        questionDto.setAggregateId(getAggregateId());
        questionDto.setVersion(getVersion());
        questionDto.setTitle(getTitle());
        questionDto.setContent(getContent());

        return questionDto;
    }
    
    public static void syncQuestionVersions(Set<QuizQuestion> prevQuestions, Set<QuizQuestion> v1Questions, Set<QuizQuestion> v2Questions) {
        for (QuizQuestion qq1 : v1Questions) {
            for (QuizQuestion qq2 : v2Questions) {
                if (qq1.getAggregateId().equals(qq2.getAggregateId())) {
                    if (qq1.getVersion() > qq2.getVersion()) {
                        qq2.setVersion(qq1.getVersion());
                        qq2.setTitle(qq1.getTitle());
                        qq2.setContent(qq1.getContent());
                        qq2.setSequence(qq1.getSequence());

                    }

                    if (qq2.getVersion() > qq1.getVersion()) {
                        qq1.setVersion(qq2.getVersion());
                        qq1.setTitle(qq2.getTitle());
                        qq1.setContent(qq2.getContent());
                        qq1.setSequence(qq2.getSequence());
                    }
                }
            }

            // no need to check again because the prev does not contain any newer version than v1 an v2
            for (QuizQuestion prevQuestion : prevQuestions) {
                if (qq1.getAggregateId().equals(prevQuestion.getAggregateId())) {
                    if (qq1.getVersion() > prevQuestion.getVersion()) {
                        prevQuestion.setVersion(qq1.getVersion());
                        prevQuestion.setTitle(qq1.getTitle());
                        prevQuestion.setContent(qq1.getContent());
                        prevQuestion.setSequence(qq1.getSequence());


                    }

                    if (prevQuestion.getVersion() > qq1.getVersion()) {
                        qq1.setVersion(prevQuestion.getVersion());
                        qq1.setTitle(prevQuestion.getTitle());
                        qq1.setContent(prevQuestion.getContent());
                        qq1.setSequence(prevQuestion.getSequence());
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof QuizQuestion)) {
            return false;
        }
        QuizQuestion quizQuestion = (QuizQuestion) obj;
        return getAggregateId() != null && getAggregateId().equals(quizQuestion.getAggregateId()) &&
                getVersion() != null && getVersion().equals(quizQuestion.getVersion());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getAggregateId();
        hash = 31 * hash + (getVersion() == null ? 0 : getVersion().hashCode());
        return hash;
    }

}
