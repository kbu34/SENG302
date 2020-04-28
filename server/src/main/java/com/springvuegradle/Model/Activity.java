package com.springvuegradle.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.type.CalendarTimeType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Activity {
    /**
     * Holds the automatically generated activity id assigned when the object is saved to the database.
     */
    @Id
    @GeneratedValue
    private long id;

    /**
     * Holds the activity name its referring to.
     */
    @Column
    @NotNull
    private String activityName;

    @NotNull
    private String description;

    @NotNull
    private Boolean continuous;

    @NotNull
    private CalendarTimeType startTime;

    @NotNull
    private CalendarTimeType endTime;

    private String location;

    /**
     * Each activity object can have multiple activities.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "activity_activity_type",
            inverseJoinColumns = @JoinColumn(name = "activity_type_id", referencedColumnName = "id"),
            joinColumns = @JoinColumn(name = "activity_id", referencedColumnName = "id"))
    private Set<ActivityType> activityTypes;


    public Activity(){}

    @JsonCreator
    public Activity(
            @JsonProperty("activity_name") String activityName,
            @JsonProperty("description") String description,
            @JsonProperty("activity_type") String[] activityTypes,
            @JsonProperty("continuous") Boolean continuous,
            @JsonProperty("start_time") CalendarTimeType startTime,
            @JsonProperty("end_time") CalendarTimeType endTime,
            @JsonProperty("location") String location){
        this.activityName = activityName;
        this.description = description;
        this.activityTypes = new HashSet<>();
        for (String activityType: activityTypes) {
            addActivityType(new ActivityType(activityType));
        }
        this.continuous = continuous;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activity activity = (Activity) o;
        return activityName.equals(activity.activityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityName);
    }


    public String getActivityName() {
        return activityName;
    }

    public void addActivityType(ActivityType activityType) {
        activityTypes.add(activityType);
    }

}