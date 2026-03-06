package pt.ulisboa.tecnico.socialsoftware.humanaethica.shift.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.shift.dto.ShiftDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

@DataJpaTest
class CreateShiftMethodTest extends SpockTest {

    private Activity makeActivity(
            Activity.State state = Activity.State.APPROVED,
            def startingDate = IN_ONE_DAY,
            def endingDate = IN_THREE_DAYS,
            int activityLimit = 5,
            List shifts = []) {

        def activity = Mock(Activity)
        activity.getState() >> state
        activity.getStartingDate() >> startingDate
        activity.getEndingDate() >> endingDate
        activity.getParticipantsNumberLimit() >> activityLimit
        activity.getShifts() >> shifts
        return activity
    }

    private ShiftDto makeValidShiftDto(
            def start = IN_TWO_DAYS,
            def end = IN_THREE_DAYS.minusHours(1),
            Integer participantsLimit = 2,
            String location = "Valid shift location text with more than twenty chars") {

        def dto = new ShiftDto()
        dto.startTime = DateHandler.toISOString(start)
        dto.endTime = DateHandler.toISOString(end)
        dto.participantsLimit = participantsLimit
        dto.location = location
        return dto
    }

    def "create valid shift"() {
        given:
        def activity = makeActivity()
        def shiftDto = makeValidShiftDto()

        when:
        def result = new Shift(activity, shiftDto)

        then:
        result.getActivity() == activity
        result.getStartTime() == IN_TWO_DAYS
        result.getEndTime() == IN_THREE_DAYS.minusHours(1)
        result.getParticipantsLimit() == 2
        result.getLocation() == "Valid shift location text with more than twenty chars"
    }

   @Unroll
def "create shift and violate required/format invariants: start=#start | end=#end | participants=#participants | location=#location"() {
    given:
    def activity = makeActivity()
    def shiftDto = new ShiftDto()
    shiftDto.startTime = start
    shiftDto.endTime = end
    shiftDto.participantsLimit = participants
    shiftDto.location = location

    when:
    new Shift(activity, shiftDto)

    then:
    def error = thrown(HEException)
    error.getErrorMessage() == errorMessage

    where:
    start                                 | end                                      | participants | location                                                   || errorMessage
    null                                  | DateHandler.toISOString(IN_THREE_DAYS)   | 1            | "Valid shift location text with more than twenty chars"    || ErrorMessage.SHIFT_INVALID_DATE
    " "                                   | DateHandler.toISOString(IN_THREE_DAYS)   | 1            | "Valid shift location text with more than twenty chars"    || ErrorMessage.SHIFT_INVALID_DATE
    "2024-01-01"                          | DateHandler.toISOString(IN_THREE_DAYS)   | 1            | "Valid shift location text with more than twenty chars"    || ErrorMessage.SHIFT_INVALID_DATE

    DateHandler.toISOString(IN_TWO_DAYS)  | null                                     | 1            | "Valid shift location text with more than twenty chars"    || ErrorMessage.SHIFT_INVALID_DATE
    DateHandler.toISOString(IN_TWO_DAYS)  | " "                                      | 1            | "Valid shift location text with more than twenty chars"    || ErrorMessage.SHIFT_INVALID_DATE
    DateHandler.toISOString(IN_TWO_DAYS)  | "12:00:00"                               | 1            | "Valid shift location text with more than twenty chars"    || ErrorMessage.SHIFT_INVALID_DATE

    DateHandler.toISOString(IN_TWO_DAYS)  | DateHandler.toISOString(IN_THREE_DAYS)   | null         | "Valid shift location text with more than twenty chars"    || ErrorMessage.SHIFT_PARTICIPANTS_LIMIT_REQUIRED
    DateHandler.toISOString(IN_TWO_DAYS)  | DateHandler.toISOString(IN_THREE_DAYS)   | 1            | null                                                       || ErrorMessage.SHIFT_LOCATION_INVALID
    DateHandler.toISOString(IN_TWO_DAYS)  | DateHandler.toISOString(IN_THREE_DAYS)   | 1            | " "                                                        || ErrorMessage.SHIFT_LOCATION_INVALID

    DateHandler.toISOString(IN_TWO_DAYS)  | DateHandler.toISOString(IN_THREE_DAYS)   | 1            | ("a" * 19)                                                  || ErrorMessage.SHIFT_LOCATION_INVALID
    DateHandler.toISOString(IN_TWO_DAYS)  | DateHandler.toISOString(IN_THREE_DAYS)   | 1            | (" " + ("a" * 19) + " ")                                    || ErrorMessage.SHIFT_LOCATION_INVALID
    DateHandler.toISOString(IN_TWO_DAYS)  | DateHandler.toISOString(IN_THREE_DAYS)   | 1            | ("   " + ("a" * 19) + "   ")                                || ErrorMessage.SHIFT_LOCATION_INVALID

    DateHandler.toISOString(IN_TWO_DAYS)  | DateHandler.toISOString(IN_THREE_DAYS)   | 1            | ("a" * 201)                                                 || ErrorMessage.SHIFT_LOCATION_INVALID
    DateHandler.toISOString(IN_TWO_DAYS)  | DateHandler.toISOString(IN_THREE_DAYS)   | 1            | (" " + ("a" * 201) + " ")                                   || ErrorMessage.SHIFT_LOCATION_INVALID
    DateHandler.toISOString(IN_TWO_DAYS)  | DateHandler.toISOString(IN_THREE_DAYS)   | 1            | ("   " + ("a" * 201) + "   ")                               || ErrorMessage.SHIFT_LOCATION_INVALID
}


    @Unroll
    def "create shift and violate start before end invariant: start=#start | end=#end"() {
        given:
        def activity = makeActivity()
        def shiftDto = makeValidShiftDto(start, end, 1)

        when:
        new Shift(activity, shiftDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.SHIFT_START_AFTER_END

        where:
        start         | end
        IN_TWO_DAYS   | IN_TWO_DAYS
        IN_THREE_DAYS | IN_TWO_DAYS
    }

    @Unroll
    def "create shift and violate shift within activity period invariant: start=#start | end=#end"() {
        given:
        def activity = makeActivity() // approved, IN_ONE_DAY .. IN_THREE_DAYS
        def shiftDto = makeValidShiftDto(start, end, 1)

        when:
        new Shift(activity, shiftDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.SHIFT_OUTSIDE_ACTIVITY_PERIOD

        where:
        start       | end
        NOW         | IN_TWO_DAYS
        IN_TWO_DAYS | IN_THREE_DAYS
        NOW         | IN_THREE_DAYS
    }

    @Unroll
    def "create shift and violate activity approved invariant: activityState=#activityState"() {
        given:
        def activity = makeActivity(activityState, IN_ONE_DAY, IN_THREE_DAYS, 5, [])
        def shiftDto = makeValidShiftDto(IN_TWO_DAYS, IN_THREE_DAYS.minusHours(1), 1)

        when:
        new Shift(activity, shiftDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.SHIFT_ACTIVITY_NOT_APPROVED

        where:
        activityState << [Activity.State.REPORTED, Activity.State.SUSPENDED]
    }

    @Unroll
    def "create shift and violate total shift limits <= activity participants limit invariant: existing=#existingLimit new=#newLimit activityLimit=#activityLimit"() {
        given:
        def otherShift = Mock(Shift)
        otherShift.getParticipantsLimit() >> existingLimit

        def activity = makeActivity(Activity.State.APPROVED, IN_ONE_DAY, IN_THREE_DAYS, activityLimit, [otherShift])
        def shiftDto = makeValidShiftDto(IN_TWO_DAYS, IN_THREE_DAYS.minusHours(1), newLimit)

        when:
        new Shift(activity, shiftDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.SHIFT_LIMITS_EXCEED_ACTIVITY_LIMIT

        where:
        existingLimit | newLimit | activityLimit
        4             | 2        | 5
        3             | 3        | 5
        5             | 1        | 5
    }

    def "create shift and violate activity required invariant: activity is null"() {
    given:
    def shiftDto = makeValidShiftDto()

    when:
    new Shift(null, shiftDto)

    then:
    def error = thrown(HEException)
    error.getErrorMessage() == ErrorMessage.SHIFT_ACTIVITY_REQUIRED
}

@Unroll
def "create shift and violate participantsLimit > 0 invariant: participantsLimit=#participantsLimit"() {
    given:
    def activity = makeActivity()
    def shiftDto = makeValidShiftDto(IN_TWO_DAYS, IN_THREE_DAYS.minusHours(1), participantsLimit)

    when:
    new Shift(activity, shiftDto)

    then:
    def error = thrown(HEException)
    error.getErrorMessage() == ErrorMessage.SHIFT_PARTICIPANTS_LIMIT_INVALID

    where:
    participantsLimit << [0, -1, -10]
}

def "create shift with participantsLimit == 1 does not throw"() {
    given:
    def activity = makeActivity()
    def shiftDto = makeValidShiftDto(IN_TWO_DAYS, IN_THREE_DAYS.minusHours(1), 1)

    when:
    new Shift(activity, shiftDto)

    then:
    noExceptionThrown()
}

def "create shift does not throw when currentSum + newLimit equals activity limit"() {
    given:
    def otherShift = Mock(Shift)
    otherShift.getParticipantsLimit() >> 3

    def activity = makeActivity(Activity.State.APPROVED, IN_ONE_DAY, IN_THREE_DAYS, 5, [otherShift])
    def shiftDto = makeValidShiftDto(IN_TWO_DAYS, IN_THREE_DAYS.minusHours(1), 2)

    when:
    new Shift(activity, shiftDto)

    then:
    noExceptionThrown() // 3 + 2 == 5 is allowed
}



    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}