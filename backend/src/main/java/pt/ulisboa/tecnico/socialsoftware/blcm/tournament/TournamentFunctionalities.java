package pt.ulisboa.tecnico.socialsoftware.blcm.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.TopicService;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.aggregate.service.AggregateIdGeneratorService;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizService;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.service.UserService;
import pt.ulisboa.tecnico.socialsoftware.blcm.version.service.VersionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;

import java.util.HashSet;
import java.util.Set;

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

        CourseExecutionDto courseExecutionDto = courseExecutionService.getCourseExecutionById(executionId, unitOfWork);
        TournamentCourseExecution tournamentCourseExecution = new TournamentCourseExecution(courseExecutionDto.getId(),
                courseExecutionDto.getCourseId(), courseExecutionDto.getAcronym(), courseExecutionDto.getStatus());

        Set<TournamentTopic> tournamentTopics = new HashSet<>();
        topicsId.forEach(topicId -> {
            TopicDto topicDto = topicService.getTopicById(topicId, unitOfWork);
            tournamentTopics.add(new TournamentTopic(topicDto.getId(), topicDto.getName(), topicDto.getCourseId()));
        });

        QuizDto quizDto = quizService.generateQuiz(tournamentDto.getNumberOfQuestions(), topicsId, unitOfWork);
        TournamentDto tournamentDto2 = tournamentService.createTournament(tournamentDto, creator, tournamentCourseExecution, tournamentTopics, new TournamentQuiz(quizDto.getId()), new UnitOfWork());


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
}
