package pt.ulisboa.tecnico.socialsoftware.blcm.quiz.domain;

import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.AggregateComponent;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipant;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.TournamentParticipantAnswer;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class QuizQuestion extends AggregateComponent {
    private String title;

    private String content;

    private Integer sequence;

    private Aggregate.AggregateState state;


    public QuizQuestion() {
        super();
    }


    public QuizQuestion(QuestionDto questionDto) {
        super(questionDto.getAggregateId(), questionDto.getVersion());
        setTitle(questionDto.getTitle());
        setContent(questionDto.getContent());
        setSequence(questionDto.getSequence());
        setState(Aggregate.AggregateState.ACTIVE);
    }

    public QuizQuestion(QuizQuestion other) {
        super(other.getAggregateId(), other.getVersion());
        setAggregateId(other.getAggregateId());
        setVersion(other.getVersion());
        setTitle(other.getTitle());
        setContent(other.getContent());
        setSequence(other.getSequence());
        setState(other.getState());
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
