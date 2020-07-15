package com.springvuegradle.Controller.enums;

import java.util.Enumeration;

public enum EmailResponseMessage {
    DUPLICATE_EMAILS("Duplicate email addresses detected."),
    EMAIL_NOT_FOUND("No profiles with that email exists."),
    NULL_PRIMARY_EMAIL("Primary address cannot be null."),
    TAKEN_EMAIL("An email address you have entered is already in use by another Profile."),
    TAKEN_PRIMARY_EMAIL("Primary email address already in use by another profile."),
    TOO_MANY_EMAILS("Cannot have more than 5 emails associated to a profile."),
    EDIT_SUCCESS("Profile emails edited successfully.");

    private String message;

    private EmailResponseMessage(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "EmailResponseMessage{" +
                "message='" + message + '\'' +
                '}';
    }
}
