package pt.ulisboa.tecnico.socialsoftware.humanaethica.shift.dto;


import pt.ulisboa.tecnico.socialsoftware.humanaethica.shift.domain.Shift;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;


public class ShiftDto {
    private Integer id;
    private Integer activityId;
    private String startTime;
    private String endTime;
    private Integer participantsLimit;
    private String location;

    public ShiftDto() {
    }

    public ShiftDto(Shift shift) {
        setId(shift.getId());
        setActivityId(shift.getActivity().getId());
        setStartTime(DateHandler.toISOString(shift.getStartTime()));
        setEndTime(DateHandler.toISOString(shift.getEndTime()));
        setParticipantsLimit(shift.getParticipantsLimit());
        setLocation(shift.getLocation());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }


    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getParticipantsLimit() {
        return participantsLimit;
    }

    public void setParticipantsLimit(Integer participantsLimit) {
        this.participantsLimit = participantsLimit;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
