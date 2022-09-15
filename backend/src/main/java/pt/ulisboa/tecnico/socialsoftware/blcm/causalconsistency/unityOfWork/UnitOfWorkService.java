package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.repository.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.repository.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.service.VersionService;

import javax.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Service
public class UnitOfWorkService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private VersionService versionService;

    @Autowired
    private EventRepository eventRepository;




    /* executes after all services have been instantiated and all fields have been injected*/
    /*@PostLoad
    public void init() {
        setVersion(versionService.getVersionNumber());
    }
    */

    @Transactional
    public UnitOfWork createUnitOfWork() {
        return new UnitOfWork(versionService.getVersionNumber());
    }


    // TODO store type in aggregate



    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void commit(UnitOfWork unitOfWork) {
        boolean concurrentAggregates = true;

        // TODO STEP 1 check whether any of the aggregates to write have concurrent versions
        // TODO STEP 2 if so perform any merges necessary
        // TODO STEP 3 performs steps 1 and 2 until step 1 stops holding
        // TODO STEP 4 perform a commit of the aggregates under SERIALIZABLE isolation

        Map<Integer, Aggregate> aggregatesToCommit = new HashMap<>(unitOfWork.getUpdatedObjectsMap());
        while (concurrentAggregates) {
            concurrentAggregates = false;
            for (Integer aggregateId : aggregatesToCommit.keySet()) {
                Aggregate aggregateToWrite = aggregatesToCommit.get(aggregateId);
                Aggregate concurrentAggregate = getConcurrentAggregate(aggregateToWrite, unitOfWork.getVersion());
                if(concurrentAggregate != null) {
                    concurrentAggregates = true;
                    Aggregate newAggregate = aggregateToWrite.merge(concurrentAggregate);
                    newAggregate.verifyInvariants();
                    newAggregate.setId(null);
                    aggregatesToCommit.put(aggregateId, newAggregate);
                }
            }

            if (concurrentAggregates) {
                // TODO because there was a concurrent version we need to get a new version
                // the service to get a new version must also increment it to guarantee two transactions do run with the same version number
                // a number must be requested every time a concurrent version is detected
                unitOfWork.setVersion(versionService.getVersionNumber());
            }
        }


        commitAllObjects(unitOfWork.getVersion(), aggregatesToCommit);
        unitOfWork.getEventsToEmit().forEach(e -> eventRepository.save(e));
    }


    // Must be serializable in order to ensure no other commits are made between the checking of concurrent versions and the actual persist
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    void commitAllObjects(Integer commitVersion, Map<Integer, Aggregate> aggregateMap) {
        aggregateMap.values().forEach(aggregateToWrite -> {
            aggregateToWrite.setVersion(commitVersion);
            aggregateToWrite.setCreationTs(LocalDateTime.now());
            entityManager.persist(aggregateToWrite);
        });
    }

    private Aggregate getConcurrentAggregate(Aggregate aggregate, Integer version) {
        Aggregate concurrentAggregate;
        switch (aggregate.getAggregateType()) {
            case COURSE:
                concurrentAggregate = courseRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;
            case COURSE_EXECUTION:
                concurrentAggregate = courseExecutionRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;
            case USER:
                concurrentAggregate = userRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;
            case TOPIC:
                concurrentAggregate = topicRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;
            case QUESTION:
                concurrentAggregate = questionRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;
            case QUIZ:
                concurrentAggregate = quizRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;
            case TOURNAMENT:
                concurrentAggregate = tournamentRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;
            default:
                throw new TutorException(INVALID_AGGREGATE_TYPE, aggregate.getClass().getSimpleName());
        }

        if(concurrentAggregate != null && concurrentAggregate.getState().equals(DELETED)) {
            throw new TutorException(ErrorMessage.AGGREGATE_DELETED, concurrentAggregate.getAggregateId());
        }

        return concurrentAggregate;
    }
}
