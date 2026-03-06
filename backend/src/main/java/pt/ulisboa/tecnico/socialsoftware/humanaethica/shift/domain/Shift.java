
package pt.ulisboa.tecnico.socialsoftware.humanaethica.shift.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.shift.dto.ShiftDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;


@Entity
@Table(name = "shift")
public class Shift {
    private static final int MIN_STRING_SIZE = 20;
    private static final int MAX_STRING_SIZE = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer participantsLimit;
    private String location;

    @ManyToOne
    private Activity activity;

    public Shift() {}

    public Shift(Activity activity, ShiftDto shiftDto) {
    this.activity = activity;
    this.startTime = DateHandler.toLocalDateTime(shiftDto.getStartTime());
    this.endTime = DateHandler.toLocalDateTime(shiftDto.getEndTime());
    this.participantsLimit = shiftDto.getParticipantsLimit();
    this.location = shiftDto.getLocation();

    verifyInvariants();
}

    private void verifyInvariants() {
        activityIsRequired();
        shiftOnlyInApprovedActivity();

        startTimeIsRequired();
        endTimeIsRequired();
        participantsLimitIsRequired();
        locationIsRequired();

        locationTextSizeBetween20And200();
        startBeforeEnd();
        shiftWithinActivityPeriod();
        participantsLimitGreaterThanZero();

        totalShiftLimitsDoNotExceedActivityLimit();
    }

    private void activityIsRequired() {
        if (this.activity == null) {
            throw new HEException(SHIFT_ACTIVITY_REQUIRED);
        }
    }

    private void shiftOnlyInApprovedActivity() {
        if (this.activity.getState() != Activity.State.APPROVED) {
            throw new HEException(SHIFT_ACTIVITY_NOT_APPROVED);
        }
    }

    private void startTimeIsRequired() {
        if (this.startTime == null) {
            throw new HEException(SHIFT_INVALID_DATE, "startTime");
        }
    }

    private void endTimeIsRequired() {
        if (this.endTime == null) {
            throw new HEException(SHIFT_INVALID_DATE, "endTime");
        }
    }

    private void participantsLimitIsRequired() {
        if (this.participantsLimit == null) {
            throw new HEException(SHIFT_PARTICIPANTS_LIMIT_REQUIRED);
        }
    }

    private void locationIsRequired() {
        if (this.location == null || this.location.trim().isEmpty()) {
            throw new HEException(SHIFT_LOCATION_INVALID);
        }
    }

    private void locationTextSizeBetween20And200() {
        int size = this.location.trim().length();
        if (size < MIN_STRING_SIZE || size > MAX_STRING_SIZE) {
            throw new HEException(SHIFT_LOCATION_INVALID);
        }
    }

    private void startBeforeEnd() {
        if (!this.startTime.isBefore(this.endTime)) {
            throw new HEException(SHIFT_START_AFTER_END);
        }
    }

    private void shiftWithinActivityPeriod() {
    var aStart = this.activity.getStartingDate();
    var aEnd = this.activity.getEndingDate();

        if (!this.startTime.isAfter(aStart) || !this.endTime.isBefore(aEnd)) {
            throw new HEException(SHIFT_OUTSIDE_ACTIVITY_PERIOD);
        }
    }

    private void participantsLimitGreaterThanZero() {
        if (this.participantsLimit <= 0) {
            throw new HEException(SHIFT_PARTICIPANTS_LIMIT_INVALID);
        }
    }

    private void totalShiftLimitsDoNotExceedActivityLimit() {
        int currentSum = this.activity.getShifts().stream()
                .mapToInt(Shift::getParticipantsLimit)
                .sum();

        if (currentSum + this.participantsLimit > this.activity.getParticipantsNumberLimit()) {
            throw new HEException(SHIFT_LIMITS_EXCEED_ACTIVITY_LIMIT);
        }
    }

    // getters

    public Integer getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }


    public LocalDateTime getEndTime() {
        return endTime;
    }


    public Integer getParticipantsLimit() {
        return participantsLimit;
    }


    public String getLocation() {
        return location;
    }


    public Activity getActivity() {
        return activity;
    }


}