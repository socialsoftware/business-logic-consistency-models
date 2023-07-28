package pt.ulisboa.tecnico.socialsoftware.ms

import pt.ulisboa.tecnico.socialsoftware.ms.quizzes.utils.DateHandler
import spock.lang.Specification

import java.time.LocalDateTime

class SpockTest extends Specification {
    public static final String ANONYMOUS = "ANONYMOUS"

    public static final LocalDateTime TIME_1 = DateHandler.now().plusMinutes(5)
    public static final LocalDateTime TIME_2 = DateHandler.now().plusMinutes(25)
    public static final LocalDateTime TIME_3 = DateHandler.now().plusHours(1).plusMinutes(5)
    public static final LocalDateTime TIME_4 = DateHandler.now().plusHours(1).plusMinutes(25)
}
