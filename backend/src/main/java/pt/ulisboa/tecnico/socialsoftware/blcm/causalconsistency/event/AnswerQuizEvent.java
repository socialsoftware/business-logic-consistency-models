package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event;

import pt.ulisboa.tecnico.socialsoftware.blcm.answer.dto.QuestionAnswerDto;

import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import java.util.List;

@Entity
@DiscriminatorValue(EventType.ANSWER_QUIZ)
public class AnswerQuizEvent extends DomainEvent {

    private Integer quizAggregateId;

    @ElementCollection
    private List<QuestionAnswerDto> questionAnswerDtoList;


}
