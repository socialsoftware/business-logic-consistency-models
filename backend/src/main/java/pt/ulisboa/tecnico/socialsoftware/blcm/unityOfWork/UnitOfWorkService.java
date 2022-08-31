package pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.integration.IntegrationProperties;
import org.springframework.stereotype.Service;
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
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

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

    public Integer getVersion(UnitOfWork unitOfWork) {
        return unitOfWork.getVersion();
    }

    public void setVersion(UnitOfWork unitOfWork, Integer version) {
        unitOfWork.setVersion(version);
    }

    public Collection<AggregateIdTypePair> getUpdatedObjects(UnitOfWork unitOfWork) {
        return unitOfWork.getUpdatedObjects();
    }

    // TODO store type in aggregate
    public void addUpdatedObject(UnitOfWork unitOfWork, Aggregate aggregate, String type) {
        unitOfWork.addUpdatedObject(aggregate, type);
    }

    public Set<DomainEvent> getEventsToEmit(UnitOfWork unitOfWork) {
        return unitOfWork.getEventsToEmit();
    }

    public void addEvent(UnitOfWork unitOfWork, DomainEvent event) {
        unitOfWork.addEvent(event);
    }

    public Map<Integer, Aggregate> getCurrentReadDependencies(UnitOfWork unitOfWork) {
        return unitOfWork.getCurrentReadDependencies();
    }

    public void addCurrentReadDependency(UnitOfWork unitOfWork, Aggregate dep) {
        unitOfWork.addCurrentReadDependency(dep);
    }

    public boolean hasAggregateDep(UnitOfWork unitOfWork, Integer aggregateId) {
        return unitOfWork.hasAggregateDep(aggregateId);
    }

    public Aggregate getAggregateDep(UnitOfWork unitOfWork, Integer aggregateId) {
        return unitOfWork.getAggregateDep(aggregateId);
    }

    public void addDependency(UnitOfWork unitOfWork, Integer objAggregateId, Dependency dep) {
        unitOfWork.addDependency(objAggregateId, dep);
    }


    @Transactional
    public void commit(UnitOfWork unitOfWork) {
        Integer newVersion = unitOfWork.getVersion();
        /* initial commit */
        commitAllObjects(unitOfWork, newVersion);

        /* before the transaction ends checks if no other write was made recently */
        while(isTherePossibleConcurrentVersions(unitOfWork.getVersion())) {
            /* if so gets a new version re-executes commit actions*/
            newVersion = versionService.getVersionNumber();
            commitAllObjects(unitOfWork, newVersion + 1);
        }

        unitOfWork.getEventsToEmit().forEach(e -> eventRepository.save(e));
        versionService.incrementVersionNumber();
    }

    private void commitAllObjects(UnitOfWork unitOfWork, Integer version) {
        unitOfWork.getUpdatedObjects().forEach(obj -> {
            switch (obj.getType()) {
                case "Course":
                    Course courseToWrite = courseRepository.findById(obj.getObjectId())
                            .orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, obj.getObjectId())); /* should be aggreateId??*/
                    Course concurrentCourse = getConcurrentCourse(courseToWrite, version);

                    if(concurrentCourse != null && concurrentCourse.getState().equals(DELETED)) {
                        throw new TutorException(COURSE_DELETED, concurrentCourse.getAggregateId());
                    }
                    /* no need to commit again if as already been committed and no concurrent version exists*/
                    if(!obj.isCommitted() || concurrentCourse != null) {
                        //cast necessary due to method signature returning Aggregate
                        Course prevCourse = ((Course)courseToWrite.getPrev());
                        commitCourse(prevCourse, courseToWrite, concurrentCourse, version);
                    }
                    break;
                case "Tournament":
                    Tournament tournamentToWrite = tournamentRepository.findById(obj.getObjectId())
                            .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, obj.getObjectId())); /* should be aggreateId??*/
                    Tournament concurrentTournament = getConcurrentTournament(tournamentToWrite, version);

                    if(concurrentTournament.getState().equals(DELETED)) {
                        throw new TutorException(TOURNAMENT_DELETED, concurrentTournament.getAggregateId());
                    }
                    /* no need to commit again if as already been committed and no concurrent version exists*/
                    if(!obj.isCommitted() || concurrentTournament != null) {
                        //cast necessary due to method signature returning Aggregate
                        Tournament prevTournament = ((Tournament)tournamentToWrite.getPrev());
                        commitTournament(prevTournament, tournamentToWrite, concurrentTournament, version);
                    }
                    break;
                case "Quiz":
                    break;
                case "CourseExecution":
                    CourseExecution courseExecutionToWrite = courseExecutionRepository.findById(obj.getObjectId())
                            .orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, obj.getObjectId()));
                    CourseExecution concurrentCourseExecution = getConcurrentCourseExection(courseExecutionToWrite, version);
                    if(!obj.isCommitted() || concurrentCourseExecution != null) {
                        //cast necessary due to method signature returning Aggregate
                        CourseExecution prevExecution = ((CourseExecution)courseExecutionToWrite.getPrev());
                        commitCourseExecution(prevExecution, courseExecutionToWrite, concurrentCourseExecution, version);
                    }
                    break;
                case "User":
                    User userToWrite = userRepository.findById(obj.getObjectId())
                            .orElseThrow(() -> new TutorException(USER_NOT_FOUND, obj.getObjectId()));
                    User concurrentUser = getConcurrentUser(userToWrite, version);
                    if(!obj.isCommitted() || concurrentUser != null) {
                        //cast necessary due to method signature returning Aggregate
                        User prevUser = ((User)userToWrite.getPrev());
                        commitUser(prevUser, userToWrite, concurrentUser, version);
                    }
                    break;
                case "Topic":
                    Topic topicToWrite = topicRepository.findById(obj.getObjectId())
                            .orElseThrow(() -> new TutorException(TOPIC_NOT_FOUND, obj.getObjectId()));
                    Topic concurrentTopic = getConcurrentTopic(topicToWrite, version);
                    if(!obj.isCommitted() || concurrentTopic != null) {
                        //cast necessary due to method signature returning Aggregate
                        Topic prevTopic = ((Topic) topicToWrite.getPrev());
                        commitTopic(prevTopic, topicToWrite, concurrentTopic, version);
                    }
                    break;
                case "Question":
                    Question questionToWrite = questionRepository.findById(obj.getObjectId())
                            .orElseThrow(() -> new TutorException(ErrorMessage.QUESTION_NOT_FOUND, obj.getObjectId()));
                    Question concurentQuestion = getConcurrentQuestion(questionToWrite, version);
                    if(!obj.isCommitted() || concurentQuestion != null) {
                        //cast necessary due to method signature returning Aggregate
                        Question prevQuestion = ((Question) questionToWrite.getPrev());
                        commitQuestion(prevQuestion, questionToWrite, concurentQuestion, version);
                    }
                    break;
                default:
                    throw new TutorException(INVALID_AGGREGATE_TYPE, obj.getType());
            }
        });
    }




    private void commitCourse(Course prevCourse, Course courseToWrite, Course concurrentCourse, Integer version) {
        /* this will likely have to be a loop in the case new concurrent versions appear while this is executing */
        Course course;
        if(concurrentCourse != null) {
            course = Course.merge(prevCourse /*prev*/, concurrentCourse /*v1*/, courseToWrite/*v2*/);
        } else {
            course = new Course(courseToWrite);
        }

        if(!course.verifyInvariants()) {
            /* ABORT don't write anything???? */
            throw new TutorException(COURSE_INVALID, course.getAggregateId());
        }
        course.setVersion(version);
        course.setState(ACTIVE);
        course.setCreationTs(LocalDateTime.now());

        courseRepository.save(course);
    }

    private void commitTournament(Tournament prevTournament, Tournament tournamentToWrite, Tournament concurrentTournament, Integer version) {
        /* this will likely have to be a loop in the case new concurrent versions appear while this is executing */
        Tournament finalTournament;
        if(concurrentTournament != null) {
            finalTournament = Tournament.merge(prevTournament /*prev*/, concurrentTournament /*v1*/, tournamentToWrite/*v2*/);
        } else {
            finalTournament = new Tournament(tournamentToWrite);
        }

        if(!finalTournament.verifyInvariants()) {
            /* ABORT don't write anything???? */
            throw new TutorException(TOURNAMENT_INVALID, finalTournament.getId());
        }
        finalTournament.setVersion(version);
        finalTournament.setState(ACTIVE);
        finalTournament.setCreationTs(LocalDateTime.now());

        tournamentRepository.save(finalTournament);
    }

    private void commitCourseExecution(CourseExecution prevCourseExecution, CourseExecution courseExecutionToWrite, CourseExecution concurrentCourseExecution, Integer version) {
        /* this will likely have to be a loop in the case new concurrent versions appear while this is executing */
        CourseExecution finalCourseExecution;
        if(concurrentCourseExecution != null) {
            finalCourseExecution = CourseExecution.merge(prevCourseExecution /*prev*/, concurrentCourseExecution /*v1*/, courseExecutionToWrite/*v2*/);
        } else {
            finalCourseExecution = new CourseExecution(courseExecutionToWrite);
        }

        if(!finalCourseExecution.verifyInvariants()) {
            /* ABORT don't write anything???? */
            throw new TutorException(ErrorMessage.COURSE_EXECUTION_INVALID, finalCourseExecution.getAggregateId(), version);
        }
        finalCourseExecution.setVersion(version);
        finalCourseExecution.setState(ACTIVE);
        finalCourseExecution.setCreationTs(LocalDateTime.now());

        courseExecutionRepository.save(finalCourseExecution);
    }

    private void commitUser(User prevUser, User userToWrite, User concurrentUser, Integer version) {
        /* this will likely have to be a loop in the case new concurrent versions appear while this is executing */
        User finalUser;
        if(concurrentUser != null) {
            finalUser = User.merge(prevUser /*prev*/, concurrentUser /*v1*/, userToWrite/*v2*/);
        } else {
            finalUser = new User(userToWrite);
        }

        if(!finalUser.verifyInvariants()) {
            /* ABORT don't write anything???? */
            throw new TutorException(ErrorMessage.COURSE_EXECUTION_INVALID, finalUser.getAggregateId(), version);
        }
        finalUser.setVersion(version);
        finalUser.setState(ACTIVE);
        finalUser.setCreationTs(LocalDateTime.now());

        userRepository.save(finalUser);
    }

    private void commitTopic(Topic prevTopic, Topic topicToWrite, Topic concurrentTopic, Integer version) {
        Topic finalTopic;
        if(concurrentTopic != null) {
            finalTopic = Topic.merge(prevTopic /*prev*/, concurrentTopic /*v1*/, topicToWrite/*v2*/);
        } else {
            finalTopic = new Topic(topicToWrite);
        }

        if(!finalTopic.verifyInvariants()) {
            /* ABORT don't write anything???? */
            throw new TutorException(ErrorMessage.COURSE_EXECUTION_INVALID, finalTopic.getAggregateId(), version);
        }
        finalTopic.setVersion(version);
        finalTopic.setState(ACTIVE);
        finalTopic.setCreationTs(LocalDateTime.now());

        topicRepository.save(finalTopic);
    }

    private void commitQuestion(Question prevQuestion, Question questionToWrite, Question concurrentQuestion, Integer version) {
        Question finalQuestion;
        if(concurrentQuestion != null) {
            finalQuestion = Question.merge(prevQuestion /*prev*/, concurrentQuestion /*v1*/, questionToWrite/*v2*/);
        } else {
            finalQuestion = new Question(questionToWrite);
        }

        if(!finalQuestion.verifyInvariants()) {
            /* ABORT don't write anything???? */
            throw new TutorException(ErrorMessage.COURSE_EXECUTION_INVALID, finalQuestion.getAggregateId(), version);
        }
        finalQuestion.setVersion(version);
        finalQuestion.setState(ACTIVE);
        finalQuestion.setCreationTs(LocalDateTime.now());

        questionRepository.save(finalQuestion);
    }


    private boolean isTherePossibleConcurrentVersions(Integer currentVersion) {
        return currentVersion < versionService.getVersionNumber();
    }

    private Course getConcurrentCourse(Course course, Integer version) {
        Course concurrentCourse = courseRepository.findConcurrentVersions(course.getAggregateId(), version)
                .stream()
                .findFirst()
                .orElse(null);
        return concurrentCourse;
    }

    private Tournament getConcurrentTournament(Tournament tournament, Integer version) {
        Tournament concurrentTournament = tournamentRepository.findConcurrentVersions(tournament.getAggregateId(), version)
                .stream()
                .findFirst()
                .orElse(null);
        return concurrentTournament;
    }

    private CourseExecution getConcurrentCourseExection(CourseExecution courseExecution, Integer version) {
        CourseExecution concurrentCourseExecution = courseExecutionRepository.findConcurrentVersions(courseExecution.getAggregateId(), version)
                .stream()
                .findFirst()
                .orElse(null);
        return concurrentCourseExecution;
    }

    private User getConcurrentUser(User user, Integer version) {
        User concurrentUser = userRepository.findConcurrentVersions(user.getAggregateId(), version)
                .stream()
                .findFirst()
                .orElse(null);
        return concurrentUser;
    }

    private Topic getConcurrentTopic(Topic topic, Integer version) {
        Topic concurrentTopic = topicRepository.findConcurrentVersions(topic.getAggregateId(), version)
                .stream()
                .findFirst()
                .orElse(null);
        return concurrentTopic;
    }

    public Question getConcurrentQuestion(Question question, Integer version) {
        Question concurrentQuestion = questionRepository.findConcurrentVersions(question.getAggregateId(), version)
                .stream()
                .findFirst()
                .orElse(null);
        return concurrentQuestion;
    }
}
