package pt.ulisboa.tecnico.socialsoftware.ms.question.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.question.domain.QuestionTopic;
import pt.ulisboa.tecnico.socialsoftware.ms.topic.event.publish.UpdateTopicEvent;

public class QuestionSubscribesUpdateTopic extends EventSubscription {
    public QuestionSubscribesUpdateTopic(QuestionTopic questionTopic) {
        super(questionTopic.getTopicAggregateId(),
                questionTopic.getTopicVersion(),
                UpdateTopicEvent.class.getSimpleName());
    }

    public boolean subscribesEvent(Event event) {
         return super.subscribesEvent(event);
    }

}