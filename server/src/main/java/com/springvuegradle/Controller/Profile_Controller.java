package com.springvuegradle.Controller;

import com.springvuegradle.Model.PassportCountry;
import com.springvuegradle.Model.Profile;
import com.springvuegradle.Model.UserEmail;
import com.springvuegradle.Repositories.PassportCountryRepository;
import com.springvuegradle.Utilities.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.springvuegradle.Repositories.ProfileRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * Profile Controller Class for handling Profile Models
 */
@RestController
public class Profile_Controller {

    @Autowired
    private ProfileRepository repository;
    @Autowired
    private PassportCountryRepository pcRepository;
    private LoginController loginController = new LoginController();
    private ValidationHelper helper = new ValidationHelper();

    /**
     * Creates a new Profile object given a set of JSON data and forms a profile object based on the given data, then
     * hashes the password and adds the new data to the database.
     * @param newProfile contains data relating to the user profile we wish to add to the database.
     * @param testing set to true when this method is used for testing purposes so that it does not save the profile
     *                object to the database, only checks if its valid.
     * @return the created profile.
     */

    public ResponseEntity<String> createProfile (Profile newProfile, boolean testing, ProfileRepository repo) {
        String error = verifyProfile(newProfile);

        if (error.equals("")) {
            // case nothing goes wrong
            String hashedPassword = hashPassword(newProfile.getPassword());
            if (hashedPassword != "Hash Failed") {
                newProfile.setPassword(hashedPassword);
            }
            Set<PassportCountry> updated = new HashSet<PassportCountry>();
            for(PassportCountry passportCountry : newProfile.retrievePassportCountryObjects()){
                List<PassportCountry> result = pcRepository.findByCountryName(passportCountry.getCountryName());

                if (result.size() == 0) {
                    String body = String.format("Country {} does not exist in the database.", passportCountry.getCountryName());
                    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
                } else {
                    updated.add(result.get(0));
                }
            }
            newProfile.setPassport_countries(updated);
            if (!testing) {
                repository.save(newProfile);
            } else {
                repo.save(newProfile);
            }
            //save profile to database
            return new ResponseEntity("New profile has been created.", HttpStatus.CREATED);
        } else {
            return new ResponseEntity(error, HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/createprofile")
    public ResponseEntity<String> createProfile (@RequestBody Profile newProfile) {
        return createProfile(newProfile, false, null);
    }

    private String verifyProfile(Profile newProfile) {
        String error = "";
        if (repository.findByEmail(newProfile.getEmail()).size() > 0) {
            error += "A profile with this email already exists in the database.\n";
        }
        if (newProfile.getEmail() == "" ||
                newProfile.getEmail() == null) {
            error += "The email field is blank.\n";
        }
        if (newProfile.getFirstname() == "" ||
                newProfile.getFirstname() == null) {
            error += "The First Name field is blank.\n";
        }
        if (newProfile.getLastname() == "" ||
                newProfile.getLastname() == null) {
            error += "The Last Name field is blank.\n";
        }
        if (newProfile.getPassword().length() < 8) {
            error += "The Password is not long enough.\n";
        }
        if (newProfile.getFitness_level() > 4 || newProfile.getFitness_level() < 0) {
            error += "The fitness level isn't valid.\n";
        }
        if (newProfile.getDate_of_birth() == "" ||
                newProfile.getDate_of_birth() == null) {
            error += "The Date of Birth field is blank.\n";
        }
        if (newProfile.retrievePassportCountryObjects().size() >= 1 ) {
            Set<PassportCountry> countries = new HashSet<>();
            try {
                countries = ValidationHelper.GetRESTCountries();
            } catch (java.io.IOException e) {
                error += e.toString();
            }
            List<String> countryNames = new ArrayList<String>();
            for (PassportCountry country : countries) {
                countryNames.add(country.getCountryName());
            }
            for (PassportCountry country : newProfile.retrievePassportCountryObjects()) {
                if (!ValidationHelper.validateCountry(country, countryNames)) {
                    error += "That country doesn't exist.\n";
                }
            }
        }
        if (!((newProfile.getGender().equals("male")) ||
                (newProfile.getGender().equals("female")) ||
                (newProfile.getGender().equals("non-Binary")))) {
            error += "The Gender field must contain either 'male', 'female' or 'non-binary'.\n";
        }
        return error;
    }

    /**
     * Takes the plaintext password and hashes it
     * @param plainPassword the plaintext password to input
     * @return the hashed password
     */
    protected static String hashPassword(String plainPassword) {
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
     * Retrieves data corresponding to the given profile ID from the database.
     * @param sessionID session token to make sure user logged in
     * @param testing if true, will skip the credential check
     * @param id gets the profile object and if it exists and authorization is approved, it will return the object
     * @return the Profile object corresponding to the given ID.
     */
    public ResponseEntity<Profile> getProfile(Long id, Long sessionID, boolean testing, ProfileRepository repo) {
        if(testing || loginController.checkCredentials(id.intValue(), sessionID)) {
            Optional<Profile> profile_with_id = null;
            if (!testing) {
                profile_with_id = repository.findById(id);
            } else {
                profile_with_id = repo.findById(id);
            }
            if (profile_with_id.isPresent()) {
                return new ResponseEntity(profile_with_id.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity(null, HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/getprofile/{id}")
    public @ResponseBody ResponseEntity<Profile> getProfile(@PathVariable Long id, @RequestHeader("authorization") long sessionID) {
        return getProfile(id, sessionID, false, null);
    }


    /**
     * Takes a Profile object and finds the corresponding profile in the database, then replaces the old profile data
     * with the new profile data in the database, then updates it.
     * @param editedProfile
     * @return true if the operation was completed successfully, false otherwise.
     */
    @PostMapping("/editprofile")
    public @ResponseBody ResponseEntity<Profile> updateProfile(@RequestBody Profile editedProfile) {
        if (verifyProfile(editedProfile) != "") {
            return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
        }
        //if(loginController.checkCredentials(editedProfile.getId().intValue(), sessionID)) {
            Long profile_id = editedProfile.getId();
            Profile db_profile = repository.findById(profile_id).get();
            db_profile.updateProfile(editedProfile);

            repository.save(db_profile);

            return new ResponseEntity(db_profile, HttpStatus.OK);
        //} else {
        //    return new ResponseEntity(null, HttpStatus.UNAUTHORIZED);
        //}
    }

    @PostMapping("/editprofile/{id}/emails")
    public ResponseEntity<String> addEmails (@RequestBody UserEmail newEmails, @PathVariable Long id, @RequestHeader("authorization") long sessionID){
        if(!loginController.checkCredentials(id.intValue(), sessionID)){
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        Profile db_profile = repository.findById(id).get();
//        ArrayList<String> additionalEmails = newEmails.getAdditionalEmails();
//        db_profile.addEmails(additionalEmails);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    /**
     * Deletes a profile from the repository given that it exists in the database.
     * @param id the id of the profile to be deleted
     * @return http response code and feedback message on the result of the delete operation
     */
    @DeleteMapping(value="/deleteprofile/{id}")
    public @ResponseBody ResponseEntity<String> deleteProfile(@PathVariable Long id) {
        //if(loginController.checkCredentials(id.intValue(), sessionID)) {
            if (repository.existsById(id)) {
                Profile profile_to_delete = repository.findById(id).get();

                repository.delete(profile_to_delete);
                return new ResponseEntity<String>("The Profile does exist in the database.", HttpStatus.OK);
            } else {
                return new ResponseEntity<String>("The profile does not exist in the database.", HttpStatus.NOT_FOUND);
            }
        //} else {
        //    return new ResponseEntity<String>("Not logged in as that profile", HttpStatus.UNAUTHORIZED);
        //}
    }

    @GetMapping("/get")
    public @ResponseBody ResponseEntity<String> get() {
        return new ResponseEntity<String>("GET Response", HttpStatus.OK);
    }

    @PostMapping("/post")
    public @ResponseBody ResponseEntity<String> post() {
        return new ResponseEntity<String>("POST Response", HttpStatus.OK);
    }

    public List<Profile> findByEmail(String primary_email) {
        List<Profile> profiles_with_email = repository.findByEmail(primary_email);
        return profiles_with_email;
    }

    protected ProfileRepository getRepository() {
        return repository;
    }

    protected void clearRepository() {
        repository.deleteAll();
    }
}
