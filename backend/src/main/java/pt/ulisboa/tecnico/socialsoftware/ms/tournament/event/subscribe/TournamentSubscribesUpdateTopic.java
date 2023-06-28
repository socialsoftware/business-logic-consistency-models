package pt.ulisboa.tecnico.socialsoftware.ms.tournament.event.subscribe;

import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.ms.causalconsistency.event.EventSubscription;
import pt.ulisboa.tecnico.socialsoftware.ms.topic.event.publish.UpdateTopicEvent;
import pt.ulisboa.tecnico.socialsoftware.ms.tournament.domain.TournamentTopic;

public class TournamentSubscribesUpdateTopic extends EventSubscription {
    public TournamentSubscribesUpdateTopic(TournamentTopic tournamentTopic) {
        super(tournamentTopic.getTopicAggregateId(),
                tournamentTopic.getTopicVersion(),
                UpdateTopicEvent.class.getSimpleName());
    }

    public boolean subscribesEvent(Event event) {
         return super.subscribesEvent(event);
    }

}