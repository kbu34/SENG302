package gradle.cucumber.steps;

import com.springvuegradle.controller.ActivityController;
import com.springvuegradle.controller.LoginController;
import com.springvuegradle.controller.Profile_Controller;
import com.springvuegradle.dto.LoginRequest;
import com.springvuegradle.dto.LoginResponse;
import com.springvuegradle.model.Activity;
import com.springvuegradle.model.ActivityMembership;
import com.springvuegradle.model.ActivityType;
import com.springvuegradle.model.Profile;
import com.springvuegradle.repositories.*;
import com.springvuegradle.service.ProfileService;
import com.springvuegradle.utilities.JwtUtil;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class ActivityTestSteps {

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    ProfileService profileService;

    @Autowired
    EmailRepository emailRepository;

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    ActivityMembershipRepository membershipRepository;

    @Autowired
    Profile_Controller profileController;

    @Autowired
    ActivityController activityController;

    @Autowired
    LoginController loginController;

    @Autowired
    ActivityTypeRepository typeRepository;

    @Autowired
    JwtUtil jwtUtil;

    private Profile profile;

    private LoginResponse loginResponse;

    private ResponseEntity<String> responseEntity;

    private Activity activity;

    @AfterEach()
    private void tearDown() {
        profileRepository.deleteAll();
        emailRepository.deleteAll();
        typeRepository.deleteAll();
        activityRepository.deleteAll();
        membershipRepository.deleteAll();
    }

    @Given("I registered account with email {string} and password {string}")
    public void i_registered_account_with_email_and_password(String email, String password) {
        profile = createNormalProfile(email, password);
        assertEquals(201, profileController.createProfile(profile).getStatusCodeValue());
        LoginRequest loginRequest = new LoginRequest(email, password);
        loginResponse = loginController.loginUser(loginRequest).getBody();
    }

    @And("I create a continuous activity with the title {string} and the location {string}")
    public void i_create_a_continuous_activity_with_the_title_with_the_activity_type_and_the_location(String title, String location) {
        typeRepository.save(new ActivityType("Running"));
        assertEquals(201, activityController.createActivity(jwtUtil.extractId(loginResponse.getToken()), activity = createNormalActivity(title, location), loginResponse.getToken()).getStatusCodeValue());
    }

    @And("An activity with the title {string} exists")
    public void an_activity_with_the_title_exists(String title) {
        assertEquals(1, activityRepository.findByActivityNames(title).size());
    }

    @When("I choose to delete the activity")
    public void i_choose_to_delete_the_activity() {
        Long activityId = activityRepository.getLastInsertedId();
        responseEntity = activityController.deleteActivity(loginResponse.getToken(), jwtUtil.extractId(loginResponse.getToken()), activityId);
    }

    @Then("The activity no longer exists")
    public void the_activity_no_longer_exists() {
        assertEquals(0, activityRepository.count());
    }

    @And("I register with email {string} and password {string} and login")
    public void i_register_with_email_and_password_and_login(String email, String password) {
        profile = createNormalProfile(email, password);
        assertEquals(201, profileController.createProfile(profile).getStatusCodeValue());
        LoginRequest loginRequest = new LoginRequest(email, password);
        loginResponse = loginController.loginUser(loginRequest).getBody();
    }

    @Then("The activity is not deleted")
    public void the_activity_is_not_deleted() {
        assertEquals(403, responseEntity.getStatusCodeValue());
        assertEquals(1, activityRepository.count());
    }

    @When("I choose to edit the activity by changing the title to {string}")
    public void i_choose_to_edit_the_activity_by_changing_the_title_to(String title) {
        activity.setActivityName(title);
        responseEntity = activityController.updateActivity(activity, loginResponse.getToken(), jwtUtil.extractId(loginResponse.getToken()), activityRepository.getLastInsertedId());

    }

    @Then("The activity is not edited")
    public void the_activity_is_not_edited() {
        assertEquals(403, responseEntity.getStatusCodeValue());
    }

    @Then("The activity was edited")
    public void the_activity_was_edited() {
        assertEquals(200, responseEntity.getStatusCodeValue());
    }

    @And("I create another account with email {string} and password {string}")
    public void i_create_another_account_with_email_and_password(String email, String password) {
        profile = createNormalProfile(email, password);
        assertEquals(201, profileController.createProfile(profile).getStatusCodeValue());
    }

    @When("I choose to add the account with the email {string} to the activity as a {string}")
    public void i_choose_to_add_the_account_with_the_email_to_the_activity_as_a(String email, String role) {
        Long profileId = profileRepository.findByPrimaryEmail(email).get(0).getId();
        System.out.println(role);
        ResponseEntity<String> response = activityController.addActivityRole(loginResponse.getToken(), profileId, activityRepository.getLastInsertedId(), role);
        System.out.println(response.getBody());
        assertEquals(201, response.getStatusCodeValue());

    }

    @Then("The activity has an organiser")
    public void the_activity_has_an_organiser() {
        assertEquals(1, membershipRepository.findActivityMembershipsByActivity_IdAndRole(activityRepository.getLastInsertedId(), ActivityMembership.Role.ORGANISER).size());
    }

    @Given("I login with the email {string} and password {string}")
    public void i_login_with_the_email_and_password(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        ResponseEntity<LoginResponse> loginResponseEntity = loginController.loginUser(loginRequest);
        loginResponse = loginResponseEntity.getBody();
        assertEquals(200, loginResponseEntity.getStatusCodeValue());
    }

    @Then("The activity has a follower")
    public void the_activity_has_a_follower() {
        assertEquals(1, membershipRepository.findActivityMembershipsByActivity_IdAndRole(activityRepository.getLastInsertedId(), ActivityMembership.Role.FOLLOWER).size());
    }


    private Profile createNormalProfile(String email, String password) {
        return new Profile(1L, "Testfname", "Testlname", "Middlenametest", "Nicknametest", email, new String[]{}, password,
                "The quick brown fox jumped over the lazy dog.", new GregorianCalendar(1999, Calendar.NOVEMBER,
                28), "male", 1, new String[]{}, new String[]{});
    }

    static Activity createNormalActivity(String title, String location) {
        return new Activity(title, "description doesn't matter atm",
                new String[]{"Running"}, true, "2020-02-20T08:00:00+1300", "2020-02-20T08:00:00+1300", location);
    }
}
