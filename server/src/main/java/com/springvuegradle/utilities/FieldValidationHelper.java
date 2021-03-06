package com.springvuegradle.utilities;

import com.springvuegradle.model.*;
import com.springvuegradle.repositories.ActivityTypeRepository;
import com.springvuegradle.repositories.EmailRepository;
import com.springvuegradle.repositories.PassportCountryRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class FieldValidationHelper {

    private FieldValidationHelper() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    /**
     * Validates the activity object by making the checks:
     * - if duration based, start and end times not null
     * - if start time selected, end time also selected (vice versa)
     * - start time comes before end time
     *
     * @param activity the activity object we want validated
     * @return string containing a description of the error in validation (if error occurs)
     */
    public static String validateActivity(Activity activity) {
        String result = "";
        if (!activity.getContinuous() && activity.getStartTime() == null && activity.getEndTime() == null) {
            result += "If a duration based activity is chosen, it must have start and end times.\n";
        }
        if (activity.getStartTime() != null && activity.getEndTime() == null) {
            result += "Start time selected, but end time is not selected.\n";
        }
        if (activity.getStartTime() == null && activity.getEndTime() != null) {
            result += "End time selected, but start time is not selected.\n";
        }
        if (activity.getStartTime() != null && activity.getEndTime() != null &&
                activity.getStartTime().isAfter(activity.getEndTime())) {
            result += "The start time must come before the end time.\n";
        }
        return result;
    }

    /**
     * Used in the create profile and update profile methods for verification. Returns a string to let the user know what
     * was entered incorrectly.
     *
     * @param newProfile object we want to complete verification on
     * @param edit_mode  indicates whether the method is being called from the update profile method to skip email authentication
     * @param pcRepo     the passport country repository
     * @param aRepo      the activity type repository
     * @param eRepo      the email repository
     * @return a string containing all the errors in the form, if any, else it will return an empty string
     */
    public static String verifyProfile(Profile newProfile, boolean edit_mode, PassportCountryRepository pcRepo,
                                       ActivityTypeRepository aRepo, EmailRepository eRepo) {
        StringBuilder errorBuilder = new StringBuilder();
        if (newProfile.retrievePrimaryEmail().getAddress().isBlank() ||
                newProfile.retrievePrimaryEmail().getAddress() == null) {
            errorBuilder.append("The email field is blank.\n");
        }
        if (newProfile.getFirstname().isBlank() ||
                newProfile.getFirstname() == null) {
            errorBuilder.append("The First Name field is blank.\n");
        }
        if (newProfile.getLastname().isBlank() ||
                newProfile.getLastname() == null) {
            errorBuilder.append("The Last Name field is blank.\n");
        }
        if (newProfile.getPassword().length() < 8) {
            errorBuilder.append("The Password is not long enough.\n");
        }
        if (newProfile.getFitness() > 4 || newProfile.getFitness() < 0) {
            errorBuilder.append("The fitness level isn't valid.\n");
        }
        if (newProfile.getDateOfBirth().isBlank() ||
                newProfile.getDateOfBirth() == null) {
            errorBuilder.append("The Date of Birth field is blank.\n");
        }
        if (!newProfile.getPassportObjects().isEmpty()) {
            for (PassportCountry passportCountry : newProfile.getPassportObjects()) {
                if (!pcRepo.existsByCountryName(passportCountry.getCountryName())) {
                    errorBuilder.append(String.format("Country %s does not exist in the database.%n", passportCountry.getCountryName()));
                }
            }
        }
        if (!newProfile.getActivityTypeObjects().isEmpty()) {
            for (ActivityType activityType : newProfile.getActivityTypeObjects()) {
                if (!aRepo.existsByActivityTypeName(activityType.getActivityTypeName())) {
                    errorBuilder.append(String.format("ActivityType %s does not exist in the database.%n", activityType.getActivityTypeName()));
                }
            }
        }

        if (!edit_mode) {
            errorBuilder.append(verifyEmailsInProfile(newProfile, eRepo));
        }
        if (!((newProfile.getGender().equals("male")) ||
                (newProfile.getGender().equals("female")) ||
                (newProfile.getGender().equals("non-Binary")))) {
            errorBuilder.append("The Gender field must contain either 'male', 'female' or 'non-Binary'.\n");
        }
        return errorBuilder.toString();
    }

    /**
     * Method used to verify emails and check if the emails are already associated with a profile in the database.
     *
     * @param newProfile object we want to verify the emails for
     * @return string containing error messages, if any, else empty string
     */
    private static String verifyEmailsInProfile(Profile newProfile, EmailRepository eRepo) {
        String error = "";
        if (!newProfile.retrieveEmails().isEmpty()) {
            boolean valid = true;
            ArrayList<String> emailStrings = new ArrayList<>();
            for (Email email : newProfile.retrieveEmails()) {
                if (eRepo.existsByAddress(email.getAddress())) {
                    valid = false;
                } else {
                    emailStrings.add(email.getAddress());
                }
            }
            if (emailStrings.size() != (new HashSet(emailStrings)).size()) {
                error += "There are duplicate email addresses used. User cannot enter same email multiple times in primary" +
                        " and/or additional.\n";
            } else if (!valid) {
                error += "An email address you have entered is already in use by another Profile.\n";
            }
        }
        return error;
    }

    /**
     * Checks if the string is null or empty.
     * @param string The string being checked.
     * @return True if the string is null or an empty string, false otherwise.
     */
    public static boolean isNullOrEmpty(String string) {
        if (string == null) {
            return true;
        } else return string.isEmpty();
    }

    /**
     * Checks if the collection is null or empty.
     * @param collection The collection being checked.
     * @return True if the collection is null or empty, false otherwise.
     */
    public static Boolean isNullOrEmpty(Collection<?> collection) {
        if (collection == null) {
            return true;
        } else return collection.isEmpty();
    }

    /**
     * Checks if the collection is null or empty.
     * @param array The array being checked.
     * @return True if the collection is null or empty, false otherwise.
     */
    public static Boolean isNullOrEmpty(String[] array) {
        if (array == null) {
            return true;
        } else return array.length == 0;
    }
}
