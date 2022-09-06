package pt.ulisboa.tecnico.socialsoftware.blcm.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.dto.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.execution.service.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.question.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.Quiz;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizCourseExecution;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.topic.service.TopicService;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.domain.*;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.quiz.QuizService;
import pt.ulisboa.tecnico.socialsoftware.blcm.tournament.service.TournamentService;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWorkService;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.blcm.user.service.UserService;
import pt.ulisboa.tecnico.socialsoftware.blcm.version.service.VersionService;
import pt.ulisboa.tecnico.socialsoftware.blcm.exception.TutorException;
import pt.ulisboa.tecnico.socialsoftware.blcm.unityOfWork.UnitOfWork;

import java.util.ArrayList;
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

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UnitOfWorkService unitOfWorkService;

    public TournamentDto createTournament(Integer userId, Integer executionId, List<Integer> topicsId,
                                          TournamentDto tournamentDto) {
        //unit of work code
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();

        checkInput(userId, topicsId, tournamentDto);

        UserDto userDto = userService.getUserById(userId, unitOfWork);
        TournamentCreator creator = new TournamentCreator(userDto.getAggregateId(), userDto.getName(), userDto.getUsername(), userDto.getVersion());

        CourseExecutionDto courseExecutionDto = courseExecutionService.getCausalCourseExecutionRemote(executionId, unitOfWork);
        TournamentCourseExecution tournamentCourseExecution = new TournamentCourseExecution(courseExecutionDto.getAggregateId(),
                courseExecutionDto.getCourseAggregateId(), courseExecutionDto.getAcronym(), courseExecutionDto.getStatus());


        Set<TournamentTopic> tournamentTopics = new HashSet<>();
        topicsId.forEach(topicId -> {
            TopicDto topicDto = topicService.getCausalTopicRemote(topicId, unitOfWork);
            tournamentTopics.add(new TournamentTopic(topicDto.getAggregateId(), topicDto.getName(), topicDto.getCourseId()));

        });

        QuizDto quizDto = new QuizDto();
        quizDto.setAvailableDate(tournamentDto.getStartTime());
        quizDto.setConclusionDate(tournamentDto.getEndTime());
        quizDto.setResultsDate(tournamentDto.getEndTime());

        List<QuestionDto> questionDtos = questionService.findQuestionsByTopics(topicsId, unitOfWork);


        QuizDto quizDto1 = quizService.generateQuiz(executionId, quizDto, questionDtos, tournamentDto.getNumberOfQuestions(), unitOfWork);

        TournamentDto tournamentDto2 = tournamentService.createTournament(tournamentDto, creator, tournamentCourseExecution, tournamentTopics, new TournamentQuiz(quizDto1.getAggregateId(), quizDto1.getVersion()), unitOfWork);


        unitOfWorkService.commit(unitOfWork);

        return tournamentDto2;
    }

    public TournamentDto getTournament(Integer aggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return tournamentService.getCausalTournamentRemote(aggregateId, unitOfWork);
    }

    public void joinTournament(Integer tournamentAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        UserDto userDto = userService.getUserById(userAggregateId, unitOfWork);
        TournamentParticipant participant = new TournamentParticipant(userDto.getAggregateId(), userDto.getName(), userDto.getUsername(), userDto.getVersion());
        tournamentService.joinTournament(tournamentAggregateId, participant, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void updateTournament(TournamentDto tournamentDto, Set<Integer> topicsAggregateIds) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();

        checkInput(topicsAggregateIds, tournamentDto);

        Set<TournamentTopic> tournamentTopics = new HashSet<>();
        topicsAggregateIds.forEach(topicAggregateId -> {
            TopicDto topicDto = topicService.getCausalTopicRemote(topicAggregateId, unitOfWork);
            tournamentTopics.add(new TournamentTopic(topicDto.getAggregateId(), topicDto.getName(), topicDto.getCourseId()));
        });

        TournamentDto newTournamentDto = tournamentService.updateTournament(tournamentDto, tournamentTopics, unitOfWork);

        List<QuestionDto> questionDtos = questionService.findQuestionsByTopics(new ArrayList<>(topicsAggregateIds), unitOfWork);
        QuizDto quizDto = new QuizDto();
        quizDto.setAvailableDate(newTournamentDto.getStartTime());
        quizDto.setConclusionDate(newTournamentDto.getEndTime());
        quizDto.setResultsDate(newTournamentDto.getEndTime());

        quizService.updateQuiz(new QuizDto(), questionDtos, unitOfWork);


        unitOfWorkService.commit(unitOfWork);
    }


    public List<TournamentDto> getTournamentsForCourseExecution(Integer executionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return tournamentService.getTournamentsForCourseExecution(executionAggregateId, unitOfWork);
    }

    public List<TournamentDto> getOpenedTournamentsForCourseExecution(Integer executionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return tournamentService.getOpenedTournamentsForCourseExecution(executionAggregateId, unitOfWork);
    }

    public List<TournamentDto> getClosedTournamentsForCourseExecution(Integer executionAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        return tournamentService.getClosedTournamentsForCourseExecution(executionAggregateId, unitOfWork);
    }

    public void leaveTournament(Integer tournamentAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        tournamentService.leaveTournament(tournamentAggregateId, userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void solveQuiz(Integer tournamentAggregateId, Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        TournamentDto tournamentDto = tournamentService.getCausalTournamentRemote(tournamentAggregateId, unitOfWork);
        tournamentService.solveQuiz(tournamentAggregateId, userAggregateId, unitOfWork);
        QuizDto quizDto = quizService.startTournamentQuiz(userAggregateId, tournamentDto.getQuiz().getAggregateId(), unitOfWork);

        //unitOfWork.addDependency(tournamentAggregateId, new Dependency(quizDto.getAggregateId(), "Quiz", unitOfWork.getVersion()));
        //unitOfWork.addDependency(quizDto.getAggregateId(), new Dependency(tournamentAggregateId, "Tournament", unitOfWork.getVersion()));

        unitOfWorkService.commit(unitOfWork);
    }

    public void cancelTournament(Integer tournamentAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        tournamentService.cancelTournament(tournamentAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    public void removeTournament(Integer tournamentAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        tournamentService.remove(tournamentAggregateId, unitOfWork);

        unitOfWorkService.commit(unitOfWork);
    }

    public void anonymizeUser(Integer userAggregateId) {
        UnitOfWork unitOfWork = unitOfWorkService.createUnitOfWork();
        tournamentService.anonymizeUser(userAggregateId, unitOfWork);
        unitOfWorkService.commit(unitOfWork);
    }

    private void checkInput(Integer userId, List<Integer> topicsId, TournamentDto tournamentDto) {
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
