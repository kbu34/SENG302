package com.springvuegradle.Controller;

import com.springvuegradle.Model.LoginRequest;
import com.springvuegradle.Model.Profile;
import com.springvuegradle.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.PersistenceException;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of a login controller class.
 * @author Alan Wang
 *
 */
@RestController
public class LoginController {

    @Autowired
    private ProfileRepository profileRepository;
    private Map<Long, Long> activeSessions;
    private long sessionCounter;

    public LoginController() {
        activeSessions = new HashMap<Long, Long>();
        sessionCounter = 0;
    }

    /**
     * Takes the plaintext password and hashes it
     * @param plainPassword the plaintext password to input
     * @return the hashed password
     */
    private String hashPassword(String plainPassword) {
        try {
            MessageDigest hashedPassword = MessageDigest.getInstance("SHA-256");
            return DatatypeConverter.printHexBinary(hashedPassword.digest(plainPassword.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            System.out.println(error);
        }
        String failPassword = "Hash Failed";
        return failPassword;
    }

    /**
     * Attempts to log in a user given a login request. If the credentials are correct, the user is logged
     * in and the session is recorded; otherwise, returns an error code.
     * @param request the user's email and password mapped from the request body onto a LoginRequest object
     * @return a response containing either the user's profile ID and session ID upon a successful login, or an
     * appropriate error code and status otherwise.
     */
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest request) {
        String body = null;
        HttpStatus status = null;

        List<Profile> result = profileRepository.findByEmail(request.getEmail());
        if (result.size() > 1) {
            body = "Server data error.";
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (result.size() == 0) {
            body = "Profile does not exist.";
            status = HttpStatus.UNAUTHORIZED;
        } else {
            Profile profile = result.get(0);
            String hashedPassword = hashPassword(request.getPassword());
            if (activeSessions.containsKey(profile.getId())) {
                body = "User already logged in.";
                status = HttpStatus.FORBIDDEN;
            } else if (!result.get(0).getPassword().equals(hashedPassword)) {
                body = "Incorrect email or password.";
                status = HttpStatus.UNAUTHORIZED;
            } else {
                body = String.format("Session ID: %d\n Profile ID %d", ++sessionCounter, result.get(0).getId());
                status = HttpStatus.OK;
                activeSessions.put(profile.getId(), sessionCounter);
            }
        }
        return new ResponseEntity<String>(body, status);
    }

    /**
     * Attempts to log out the user given a HTTP logout request. Only succeeds if the user's credentials are correct.
     * @param profileID the user's profile ID from the request body.
     * @param field the Authorization field in the request header.
     * @return An HTTP response with the appropriate message and HTTP code depending on the logout success
     */
    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<String> logoutUser(@RequestBody long profileID, @RequestHeader("authorization") String field){
        String message = null;
        HttpStatus status = null;
        Long sessionID = Long.parseLong(field.split(" ")[0]);
        if (checkCredentials(profileID, sessionID)){
            message = "Logout successful.";
            status = HttpStatus.OK;
            activeSessions.remove(profileID);
        } else {
            message = "Invalid session key pair.";
            status = HttpStatus.UNAUTHORIZED;
        }

        return new ResponseEntity<>(message, status);
    }

    /**
     *  Given a request's user ID and session ID, checks for a match with an existing session.
     * @param userID the user ID
     * @param sessionID the session ID to be validated
     * @return true if the session ID matches the user ID; false otherwise.
     */
    public boolean checkCredentials(long userID, long sessionID){
        if (activeSessions.containsKey(userID)) {
            return sessionID == activeSessions.get(userID);
        } else {
            return false;
        }
    }
}
