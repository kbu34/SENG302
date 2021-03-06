package com.springvuegradle.utilities;

import com.springvuegradle.dto.ActivityParticipationRequest;
import com.springvuegradle.model.Activity;
import com.springvuegradle.model.ActivityParticipation;
import com.springvuegradle.model.Profile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class ActivityTestUtils {

    /**
     * Creates a new activity with the following details:
     * <br>Name:        Kaikoura Coast Track race
     * <br>Description: A big and nice race on a lovely peninsula
     * <br>Types:       Hiking
     * <br>Continuous:  False
     * <br>Start Time:  2020-02-20 08:00:00+1300
     * <br>End Time:    2020-02-20 08:00:00+1300
     * <br>Location:    Kaikoura, NZ
     * <br>Longitude:   100.00
     * <br>Latitude:    100.00
     * @return a new activity.
     */
    public static Activity createNormalActivity() {
        return new Activity("Kaikoura Coast Track race", "A big and nice race on a lovely peninsula",
                new String[]{"Hiking"}, false, "2020-02-20T08:00:00+1300", "2020-02-20T08:00:00+1300", "Kaikoura, NZ", 100.00, 100.00);
    }

    /**
     * Creates a new activity with the following details:
     * <br>Name:        Blazer Tag
     * <br>Description: It's lazer tag in blazers
     * <br>Types:       Running
     * <br>Continuous:  True
     * <br>Start Time:  null
     * <br>End Time:    null
     * <br>Location:    Christchurch, NZ
     * <br>Longitude:   90.00
     * <br>Latitude:    90.00
     * @return a new activity.
     */
    public static Activity createNormalActivityTwo() {
        return new Activity("Blazer Tag", "It's lazer tag in blazers", new String[]{"Running"},
                true, null, null, "Christchurch, NZ", 90.00, 90.00);
    }

    /**
     * Creates a new activity with the following details:
     * <br>Name:        Bicycle Race
     * <br>Description: I WANT TO RIDE MY
     * <br>Types:       Cycling
     * <br>Continuous:  False
     * <br>Start Time:  1978-10-13 01:00:00+1300
     * <br>End Time:    1979-10-13 01:00:00+1300
     * <br>Location:    Invercargill, NZ
     * <br>Longitude:   55.00
     * <br>Latitude:    0.00
     * @return a new activity.
     */
    public static Activity createNormalActivityThree() {
        return new Activity("Bicycle Race", "I WANT TO RIDE MY", new String[]{"Cycling"},
            false, "1978-10-13T01:00:00+1300", "1979-10-13T01:00:00+1300",
                "Invercargill, NZ", 55.00, 0.00);
    }

    public static Activity createActivity(String name, Double latitude, Double longitude) {
        return new Activity(
                name,
                "A big and nice race on a lovely peninsula",
                new String[]{"Hiking"},
                false,
                "2020-02-20T08:00:00+1300",
                "2020-02-20T08:00:00+1300",
                "Kaikoura, NZ",
                latitude,
                longitude);
    }

    public static Activity updateNormalActivity(String name, String description, String[] activityTypes, Boolean continuous,
                                                String startTime, String endTime, String location, double latitude, double longitude) {
        if (name == "default") {
            name = "Kaikoura Coast Track race";
        }
        if (description == "default") {
            description = "Kaikoura Coast Track race";
        }
        if(activityTypes == null) {
            activityTypes =  new String[]{"Hiking"};
        }
        if (startTime == "default") {
            startTime = "2020-02-20T08:00:00+1300";
        }
        if (endTime == "default") {
            endTime = "2020-03-20T08:00:00+1300";
        }
        if (location == "default") {
            location = "Kaikoura, NZ";
        }
        return new Activity(name, description, activityTypes, continuous, startTime, endTime, location, latitude, longitude);
    }


    public static Activity createNormalActivitySilly() {
        return new Activity("Wibble", "A bald man", new String[]{"Hockey"}, true,
                "2020-02-20T08:00:00+1300","2020-02-20T08:00:00+1300", "K2", 100.00, 100.00);
    }

    public static Activity createNormalActivitySillyNoActivityName() {
        return new Activity(null, "A bald man", new String[]{"Hockey"}, true,
                "2020-02-20T08:00:00+1300","2020-02-20T08:00:00+1300", "K2", 100.00, 100.00);
    }

    public static Activity createDifferentLocationActivity() {
        return new Activity("Kaikoura Coast Track race", "A big and nice race on a lovely peninsula",
                new String[]{"Hiking"}, false, "2020-02-20T08:00:00+1300", "2020-02-20T08:00:00+1300", "Dunedin, NZ", 50.00, 200.00);
    }

    public static ActivityParticipation createNormalParticipation() {
        return new ActivityParticipation("Played League as a noob", "Got rekt", "2020-02-20T08:00:00+1300", "2020-02-20T08:00:00+1300");
    }

    public static ActivityParticipation createEditedNormalParticipation() {
        return new ActivityParticipation("Played League as a pro", "Won the game", "2020-02-20T12:00:00+1300", "2020-02-20T16:00:00+1300");
    }

    public static ActivityParticipation createADifferentParticipation() {
        return new ActivityParticipation("Scored last minute winner", "Won Champions League :)", "2020-02-20T08:00:00+1300", "2020-02-20T08:00:00+1300");
    }

    public static ActivityParticipationRequest createNormalParticipationRequest() {
        return new ActivityParticipationRequest("Played League as a noob", "Got rekt", "2020-02-20T08:00:00+1300", "2020-02-20T08:00:00+1300");
    }

    public static ActivityParticipationRequest createADifferentParticipationRequest() {
        return new ActivityParticipationRequest("Scored last minute winner", "Won Champions League :)", "2020-02-20T08:00:00+1300", "2020-02-20T08:00:00+1300");
    }

    public static List<ActivityParticipation> createValidActivityParticipationsList() {
        ActivityParticipation normalActivityParticpation = createNormalParticipation();
        ActivityParticipation normalActivityParticpation1 = createADifferentParticipation();
        List<ActivityParticipation> activityParticipations = new ArrayList<>();
        activityParticipations.add(normalActivityParticpation);
        activityParticipations.add(normalActivityParticpation1);
        return activityParticipations;
    }

    public static List<Activity> createListOfActivities() {
        List<Activity> activities = new ArrayList<>();
        activities.add(new Activity("Kaikoura Coast Track race", "A big and nice race on a lovely peninsula",
                new String[]{"Hiking"}, false, "2020-02-20T08:00:00+1300",
                "2020-02-20T08:00:00+1300", "Kaikoura, NZ", 100.00, 100.00));

        activities.add(new Activity("Kaikoura Coast Track race2", "A big and nice race on a lovely peninsula",
                new String[]{"Hiking"}, false, "2020-02-20T08:00:00+1300",
                "2020-02-20T08:00:00+1300", "Kaikoura, NZ", 100.00, 100.00));

        activities.add(new Activity("Kaikoura Coast Track race3", "A big and nice race on a lovely peninsula",
                new String[]{"Hiking"}, false, "2020-02-20T08:00:00+1300",
                "2020-02-20T08:00:00+1300", "Kaikoura, NZ", 100.00, 100.00));

        return activities;
    }
}
