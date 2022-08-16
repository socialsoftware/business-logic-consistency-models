package pt.ulisboa.tecnico.socialsoftware.blcm.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.TopicService;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizService;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.Dependency;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.service.UserService;
import pt.ulisboa.tecnico.socialsoftware.blcm.version.service.VersionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.blcm.exception.ErrorMessage.*;

@Service
public class TournamentFunctionalities {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseExecutionService courseExecutionService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private QuizService quizService;

    public TournamentDto createTournament(Integer userId, Integer executionId, Set<Integer> topicsId,
                                          TournamentDto tournamentDto) {
        //unit of work code
        UnitOfWork unitOfWork = new UnitOfWork();

        checkInput(userId, topicsId, tournamentDto);

        UserDto userDto = userService.getUserById(userId, unitOfWork);
        TournamentCreator creator = new TournamentCreator(userDto.getAggregateId(), userDto.getName(), userDto.getUsername());

        unitOfWork.addDependency(tournamentDto.getAggregateId(), new Dependency(userDto.getAggregateId(), "User", userDto.getVersion()));

        CourseExecutionDto courseExecutionDto = courseExecutionService.getCourseExecutionById(executionId, unitOfWork);
        TournamentCourseExecution tournamentCourseExecution = new TournamentCourseExecution(courseExecutionDto.getAggregateId(),
                courseExecutionDto.getCourseId(), courseExecutionDto.getAcronym(), courseExecutionDto.getStatus());

        unitOfWork.addDependency(tournamentDto.getAggregateId(), new Dependency(courseExecutionDto.getAggregateId(), "CourseExecution", courseExecutionDto.getVersion()));

        Set<TournamentTopic> tournamentTopics = new HashSet<>();
        topicsId.forEach(topicId -> {
            TopicDto topicDto = topicService.getTopicByAggregateId(topicId, unitOfWork);
            tournamentTopics.add(new TournamentTopic(topicDto.getAggregateId(), topicDto.getName(), topicDto.getCourseId()));

            unitOfWork.addDependency(tournamentDto.getAggregateId(), new Dependency(topicDto.getAggregateId(), "Topic", topicDto.getVersion()));
        });

        QuizDto quizDto = quizService.generateQuiz(tournamentDto.getNumberOfQuestions(), topicsId, unitOfWork);
        TournamentDto tournamentDto2 = tournamentService.createTournament(tournamentDto, creator, tournamentCourseExecution, tournamentTopics, new TournamentQuiz(quizDto.getAggregateId()), new UnitOfWork());

        unitOfWork.addDependency(tournamentDto.getAggregateId(), new Dependency(quizDto.getAggregateId(), "Quiz", quizDto.getVersion()));
        unitOfWork.addDependency(quizDto.getAggregateId(), new Dependency(tournamentDto2.getAggregateId(), "Tournament", tournamentDto2.getVersion()));

        unitOfWork.commit();

        return tournamentDto2;
    }

    public TournamentDto getTournament(Integer aggregateId) {
        UnitOfWork unitOfWork = new UnitOfWork();
        return tournamentService.getTournament(aggregateId, unitOfWork);
    }

    public void joinTournament(Integer tournamentAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = new UnitOfWork();
        UserDto userDto = userService.getUserById(userAggregateId, unitOfWork);
        TournamentParticipant participant = new TournamentParticipant(userDto.getAggregateId(), userDto.getName(), userDto.getUsername());
        tournamentService.joinTournament(tournamentAggregateId, participant, unitOfWork);
        unitOfWork.commit();
    }

    public void updateTournament(TournamentDto tournamentDto, Set<Integer> topicsAggregateIds) {
        UnitOfWork unitOfWork = new UnitOfWork();

        checkInput(topicsAggregateIds, tournamentDto);

        Set<TournamentTopic> tournamentTopics = new HashSet<>();
        topicsAggregateIds.forEach(topicAggregateId -> {
            TopicDto topicDto = topicService.getTopicByAggregateId(topicAggregateId, unitOfWork);
            tournamentTopics.add(new TournamentTopic(topicDto.getAggregateId(), topicDto.getName(), topicDto.getCourseId()));
            unitOfWork.addDependency(tournamentDto.getAggregateId(), new Dependency(topicDto.getAggregateId(), "Topic", topicDto.getVersion()));
        });

        TournamentDto newTournamentDto = tournamentService.updateTournament(tournamentDto, tournamentTopics, unitOfWork);
        QuizDto quizDto = quizService.generateQuiz(newTournamentDto.getNumberOfQuestions(), newTournamentDto.getTopics().stream().map(TournamentTopic::getAggregateId).collect(Collectors.toSet()), unitOfWork);

        unitOfWork.addDependency(tournamentDto.getAggregateId(), new Dependency(quizDto.getAggregateId(), "Quiz", quizDto.getVersion()));
        unitOfWork.addDependency(quizDto.getAggregateId(), new Dependency(newTournamentDto.getAggregateId(), "Tournament", newTournamentDto.getVersion()));

        unitOfWork.commit();
    }


    public List<TournamentDto> getTournamentsForCourseExecution(Integer executionAggregateId) {
        UnitOfWork unitOfWork = new UnitOfWork();
        return tournamentService.getTournamentsForCourseExecution(executionAggregateId, unitOfWork);
    }

    public List<TournamentDto> getOpenedTournamentsForCourseExecution(Integer executionAggregateId) {
        UnitOfWork unitOfWork = new UnitOfWork();
        return tournamentService.getOpenedTournamentsForCourseExecution(executionAggregateId, unitOfWork);
    }

    public List<TournamentDto> getClosedTournamentsForCourseExecution(Integer executionAggregateId) {
        UnitOfWork unitOfWork = new UnitOfWork();
        return tournamentService.getClosedTournamentsForCourseExecution(executionAggregateId, unitOfWork);
    }

    public void leaveTournament(Integer tournamentAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = new UnitOfWork();
        tournamentService.leaveTournament(tournamentAggregateId, userAggregateId, unitOfWork);
        unitOfWork.commit();
    }

    public void solveQuiz(Integer tournamentAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = new UnitOfWork();


        TournamentDto tournamentDto = tournamentService.getTournament(tournamentAggregateId, unitOfWork);

        tournamentService.solveQuiz(tournamentAggregateId, userAggregateId, unitOfWork);

        QuizDto quizDto = quizService.startTournamentQuiz(userAggregateId, tournamentDto.getQuiz().getAggregateId(), unitOfWork);

        unitOfWork.addDependency(tournamentAggregateId, new Dependency(quizDto.getAggregateId(), "Quiz", unitOfWork.getVersion()));

        unitOfWork.addDependency(quizDto.getAggregateId(), new Dependency(tournamentAggregateId, "Tournament", unitOfWork.getVersion()));

        unitOfWork.commit();

    }

    public void cancelTournament(Integer tournamentAggregateId) {
        UnitOfWork unitOfWork = new UnitOfWork();
        tournamentService.cancelTournament(tournamentAggregateId, unitOfWork);
        unitOfWork.commit();
    }

    public void removeTournament(Integer tournamentAggregateId) {
        UnitOfWork unitOfWork = new UnitOfWork();
        tournamentService.remove(tournamentAggregateId, unitOfWork);

        unitOfWork.commit();
    }

    public void anonymizeUser(Integer userAggregateId) {
        UnitOfWork unitOfWork = new UnitOfWork();
        tournamentService.anonymizeUser(userAggregateId, unitOfWork);
        unitOfWork.commit();
    }

    private void checkInput(Integer userId, Set<Integer> topicsId, TournamentDto tournamentDto) {
        if (userId == null) {
            throw new TutorException(TOURNAMENT_MISSING_USER);
        }
        if (topicsId == null) {
            throw new TutorException(TOURNAMENT_MISSING_TOPICS);
        }
        if (tournamentDto.getStartTime() == null) {
            throw new TutorException(TOURNAMENT_MISSING_START_TIME);
        }
        if (tournamentDto.getEndTime() == null) {
            throw new TutorException(TOURNAMENT_MISSING_END_TIME);
        }
        if (tournamentDto.getNumberOfQuestions() == null) {
            throw new TutorException(TOURNAMENT_MISSING_NUMBER_OF_QUESTIONS);
        }
    }

    private void checkInput(Set<Integer> topicsId, TournamentDto tournamentDto) {
        if (topicsId == null) {
            throw new TutorException(TOURNAMENT_MISSING_TOPICS);
        }
        if (tournamentDto.getStartTime() == null) {
            throw new TutorException(TOURNAMENT_MISSING_START_TIME);
        }
        if (tournamentDto.getEndTime() == null) {
            throw new TutorException(TOURNAMENT_MISSING_END_TIME);
        }
        if (tournamentDto.getNumberOfQuestions() == null) {
            throw new TutorException(TOURNAMENT_MISSING_NUMBER_OF_QUESTIONS);
        }
    }
}
