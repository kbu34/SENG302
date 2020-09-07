package com.springvuegradle.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.springvuegradle.enums.NotificationType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.*;


@Entity
public class Notification {
    /**
     * Holds automatically generated notification id that is assigned when the
     * object is saved to the database.
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Holds the notification message as a string.
     */
    @Column
    @NotNull
    private String message;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "activity_id")
    @JsonBackReference(value = "activity")
    private Activity activity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id")
    @JsonBackReference(value = "profile")
    private Profile profile;

    /**
     * Holds the user's notifications and estabishes a Many to Many relationship as a Profile object can be associated with
     * multiple Notifications.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "profile_notification",
            inverseJoinColumns = @JoinColumn(name = "notification_id", referencedColumnName = "id"),
            joinColumns = @JoinColumn(name = "profile_id", referencedColumnName = "id"))
    private Set<Profile> recipients = new HashSet<>();

    @Column
    private OffsetDateTime timeStamp = OffsetDateTime.now();

    @Column
    @NotNull
    private NotificationType notificationType;

    public Notification(
            String message, Activity activity, Profile profile, NotificationType notificationType)
    {
        this.message = message;
        this.activity = activity;
        this.profile = profile;
        this.notificationType = notificationType;
    };

    public Notification() {}

    @Override
    public int hashCode() {
        return Objects.hash(id, message, activity, profile, notificationType);
    }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    @JsonIgnore
    public Activity getActivity() { return activity; }

    public void setActivity(Activity activity) { this.activity = activity; }

    public Long getActivityId() {
        return activity != null ? activity.getId() : null;
    }

    public long getEditorId() { return profile.getId(); }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    @JsonIgnore
    public Set<Profile> getRecipients() {
        return Collections.unmodifiableSet(recipients);
    }

    public void setRecipients(Set<Profile> recipients) {
        this.recipients = recipients;
    }

    public boolean addRecipient(Profile recipient) {
        return recipients.add(recipient);
    }

    public boolean removeRecipient(Profile recipient) {
        return recipients.remove(recipient);
    }

}
