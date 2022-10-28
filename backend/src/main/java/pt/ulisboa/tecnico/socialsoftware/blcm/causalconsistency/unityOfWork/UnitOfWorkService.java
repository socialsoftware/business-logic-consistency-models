package pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.unityOfWork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import pt.ulisboa.tecnico.socialsoftware.blcm.answer.repository.AnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.Event;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.repository.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.event.utils.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.repository.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.version.service.VersionService;

import javax.persistence.DiscriminatorValue;
import javax.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.causalconsistency.aggregate.domain.Aggregate.AggregateState.INACTIVE;
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
    private AnswerRepository answerRepository;

    @Autowired
    private VersionService versionService;

    @Autowired
    private EventRepository eventRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UnitOfWork createUnitOfWork() {
        Integer lastCommittedAggregateVersionNumber = versionService.getVersionNumber();
        return new UnitOfWork(lastCommittedAggregateVersionNumber+1);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void commit(UnitOfWork unitOfWork) {
        boolean concurrentAggregates = true;

        // STEP 1 check whether any of the aggregates to write have concurrent versions
        // STEP 2 if so perform any merges necessary
        // STEP 3 performs steps 1 and 2 until step 1 stops holding
        // STEP 4 perform a commit of the aggregates under SERIALIZABLE isolation

        Map<Integer, Aggregate> originalAggregatesToCommit = new HashMap<>(unitOfWork.getAggregatesToCommit());

        // may contains merged aggregates
        // we do not want to compare intermediate merged aggregates with concurrent aggregate so we separate
        // the comparison is always between the original written by the functionality and the concurrent
        Map<Integer, Aggregate> modifiedAggregatesToCommit = new HashMap<>(unitOfWork.getAggregatesToCommit());

        while (concurrentAggregates) {
            concurrentAggregates = false;
            for (Integer aggregateId : originalAggregatesToCommit.keySet()) {
                Aggregate aggregateToWrite = originalAggregatesToCommit.get(aggregateId);
                if(aggregateToWrite.getPrev() != null && aggregateToWrite.getPrev().getState() == INACTIVE) {
                    throw new TutorException(CANNOT_MODIFY_INACTIVE_AGGREGATE, aggregateToWrite.getAggregateId());
                }
                aggregateToWrite.verifyInvariants();
                Aggregate concurrentAggregate = getConcurrentAggregate(aggregateToWrite, unitOfWork.getVersion());
                // second condition is necessary for when a concurrent version is detected at first and then in the following detections it will have to do
                // this verification in order to not detect the same as a version as concurrent again
                if(concurrentAggregate != null && unitOfWork.getVersion() <= concurrentAggregate.getVersion()) {
                    concurrentAggregates = true;
                    Aggregate newAggregate = aggregateToWrite.merge(concurrentAggregate);
                    newAggregate.verifyInvariants();
                    newAggregate.setId(null);
                    modifiedAggregatesToCommit.put(aggregateId, newAggregate);
                }
            }

            if (concurrentAggregates) {
                // because there was a concurrent version we need to get a new version
                // the service to get a new version must also increment it to guarantee two transactions do run with the same version number
                // a number must be requested every time a concurrent version is detected
                unitOfWork.setVersion(versionService.incrementAndGetVersionNumber());
            }
        }

        // registering the emitted events on the committed aggregates
        for(Aggregate a : modifiedAggregatesToCommit.values()) {
            for(Event e : unitOfWork.getEventsToEmit()) {
                if(a.getAggregateId().equals(e.getAggregateId())) {
                    a.addEmittedEvent(e.getClass().getAnnotation( DiscriminatorValue.class ).value(), unitOfWork.getVersion());
                }
            }
        }

        Integer commitVersion;
        if(versionService.getVersionNumber() < unitOfWork.getVersion()) {
            commitVersion = versionService.incrementAndGetVersionNumber();
        } else {
            commitVersion = unitOfWork.getVersion();
        }
        commitAllObjects(commitVersion, modifiedAggregatesToCommit);
        unitOfWork.getEventsToEmit().forEach(e -> {
            /* this is so event detectors can compare this version to those of running transactions */
            e.setAggregateVersion(commitVersion);
            eventRepository.save(e);
        });
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

        /* if the prev aggregate is null it means this is a creation functionality*/
        if(aggregate.getPrev() == null) {
            return null;
        }

        switch (aggregate.getAggregateType()) {
            case COURSE:
                concurrentAggregate = courseRepository.findConcurrentVersions(aggregate.getAggregateId(), aggregate.getPrev().getVersion())
                        .orElse(null);
                break;
            case COURSE_EXECUTION:
                concurrentAggregate = courseExecutionRepository.findConcurrentVersions(aggregate.getAggregateId(), aggregate.getPrev().getVersion())
                        .orElse(null);
                break;
            case USER:
                concurrentAggregate = userRepository.findConcurrentVersions(aggregate.getAggregateId(), aggregate.getPrev().getVersion())
                        .orElse(null);
                break;
            case TOPIC:
                concurrentAggregate = topicRepository.findConcurrentVersions(aggregate.getAggregateId(), aggregate.getPrev().getVersion())
                        .orElse(null);
                break;
            case QUESTION:
                concurrentAggregate = questionRepository.findConcurrentVersions(aggregate.getAggregateId(), aggregate.getPrev().getVersion())
                        .orElse(null);
                break;
            case QUIZ:
                concurrentAggregate = quizRepository.findConcurrentVersions(aggregate.getAggregateId(), aggregate.getPrev().getVersion())
                        .orElse(null);
                break;
            case TOURNAMENT:
                concurrentAggregate = tournamentRepository.findConcurrentVersions(aggregate.getAggregateId(), aggregate.getPrev().getVersion())
                        .orElse(null);
                break;
            case ANSWER:
                concurrentAggregate = answerRepository.findConcurrentVersions(aggregate.getAggregateId(), aggregate.getPrev().getVersion())
                        .orElse(null);
                break;
            default:
                throw new TutorException(INVALID_AGGREGATE_TYPE, aggregate.getClass().getSimpleName());
        }

        // if a concurrent version is deleted it means the object has been deleted in the meanwhile
        if(concurrentAggregate != null && (concurrentAggregate.getState() == DELETED || concurrentAggregate.getState() == INACTIVE)) {
            throw new TutorException(ErrorMessage.AGGREGATE_DELETED, concurrentAggregate.getAggregateId());
        }

        return concurrentAggregate;
    }
}
