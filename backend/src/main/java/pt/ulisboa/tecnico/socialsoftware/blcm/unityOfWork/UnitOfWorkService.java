package pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.integration.IntegrationProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.Course;
import pt.ulisboa.tecnico.socialsoftware.blcm.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.DomainEvent;
import pt.ulisboa.tecnico.socialsoftware.blcm.event.EventRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.repository.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.Question;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.blcm.version.service.VersionService;

import javax.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.ACTIVE;
import static pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.domain.Aggregate.AggregateState.DELETED;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;
import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.TOURNAMENT_INVALID;

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



    @Transactional
    public void commit(UnitOfWork unitOfWork) {
        boolean concurrentAggregates = true;
        Integer commitVersion = unitOfWork.getVersion();

        // TODO STEP 1 check whether any of the aggregates to write have concurrent versions
        // TODO STEP 2 if so perform any merges necessary
        // TODO STEP 3 performs steps 1 and 2 until step 1 stops holding
        // TODO STEP 4 perform a commit of the aggregates under SERIALIZABLE isolation


        Map<Integer, Aggregate> aggregatesToCommit = new HashMap<>(unitOfWork.getUpdatedObjectsMap());
        while (concurrentAggregates) {
            commitVersion = unitOfWork.getVersion();
            concurrentAggregates = false;
            for (Integer aggregateId : aggregatesToCommit.keySet()) {
                Aggregate aggregateToWrite = aggregatesToCommit.get(aggregateId);
                Aggregate concurrentAggregate = getConcurrentAggregate(aggregateToWrite, commitVersion);
                if(concurrentAggregate != null) {
                    concurrentAggregates = true;
                    Aggregate newAggregate = aggregateToWrite.merge(concurrentAggregate);
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


        commitAllObjects(unitOfWork, aggregatesToCommit, commitVersion);
        unitOfWork.getEventsToEmit().forEach(e -> eventRepository.save(e));
    }

    /*@Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    // Must be serializable in order to ensure on
    void commitAllObjects(UnitOfWork unitOfWork, Integer version) {
        unitOfWork.getUpdatedObjects().forEach(aggregateToWrite -> {

            Aggregate concurrentAggregate = getConcurrentAggregate(aggregateToWrite, version);

            if(concurrentAggregate != null && concurrentAggregate.getState().equals(DELETED)) {
                throw new TutorException(COURSE_DELETED, concurrentCourse.getAggregateId());
            }
            /* no need to commit again if as already been committed and no concurrent version exists*/
            /*if(!obj.isCommitted() || concurrentAggregate != null) {
                //cast necessary due to method signature returning Aggregate
                Course prevCourse = ((Course)courseToWrite.getPrev());
                commitCourse(prevCourse, courseToWrite, concurrentCourse, version);
            }

        });
    }*/

    // Must be serializable in order to ensure no other commits are made between the checking of concurrent versions and the actual persist
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    void commitAllObjects(UnitOfWork unitOfWork, Map<Integer, Aggregate> aggregateMap, Integer version) {
        aggregateMap.values().forEach(aggregateToWrite -> {
            aggregateToWrite.setVersion(unitOfWork.getVersion());
            aggregateToWrite.setCreationTs(LocalDateTime.now());
            entityManager.persist(aggregateToWrite);
        });
    }

    private Aggregate getConcurrentAggregate(Aggregate aggregate, Integer version) {
        Aggregate concurrentAggregate;
        switch (aggregate.getClass().getSimpleName()) {
            case "Course":
                concurrentAggregate = courseRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;
            case "CourseExecution":
                concurrentAggregate = courseExecutionRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;
            case "User":
                concurrentAggregate = userRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;
            case "Topic":
                concurrentAggregate = topicRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;
            case "Question":
                concurrentAggregate = questionRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;
            case "Quiz":
                /*return quizRepository.findConcurrentVersions(aggregate.getAggregateId(), version)
                        .stream()
                        .findFirst()
                        .orElse(null);
                break;*/
            case "Tournament":
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
