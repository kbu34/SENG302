package gradle.cucumber.steps;

import com.springvuegradle.controller.ActivityController;
import com.springvuegradle.controller.LoginController;
import com.springvuegradle.controller.Profile_Controller;
import com.springvuegradle.dto.requests.ActivityRoleUpdateRequest;
import com.springvuegradle.dto.requests.LoginRequest;
import com.springvuegradle.dto.responses.ActivityMemberProfileResponse;
import com.springvuegradle.dto.responses.LoginResponse;
import com.springvuegradle.dto.PrivacyRequest;
import com.springvuegradle.model.*;
import com.springvuegradle.dto.*;
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
import org.springframework.http.HttpStatus;
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

    private ResponseEntity<List<ActivityMemberProfileResponse>> expectedMemberProfileResponse;

    private ResponseEntity<List<ActivityMemberProfileResponse>> actualMemberProfileResponse;

    private ResponseEntity<String> responseEntity;

    private Activity activity;

    private SimplifiedActivitiesResponse simplifiedActivitiesResponse;

    @AfterEach()
    private void tearDown() {
        emailRepository.deleteAll();
        profileRepository.deleteAll();
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

    @When("I change the visibility of my activity to {string} as the creator with email {string}")
    public void i_change_the_visibility_of_my_activity_to_as_the_creator_with_email(String privacy, String email) {
        Long profileId = profileRepository.findByPrimaryEmail(email).get(0).getId();
        PrivacyRequest privacyRequest = new PrivacyRequest(privacy);
        ResponseEntity<String> response = activityController.editActivityPrivacy(privacyRequest, loginResponse.getToken(), profileId, activityRepository.getLastInsertedId());
        System.out.println(response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Then("The activity is public")
    public void the_activity_is_public() {
        assertEquals(2, activityRepository.getOne(activityRepository.getLastInsertedId()).getPrivacyLevel());
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

    @Then("There is one activity with privacy {string}")
    public void there_is_one_activity_with_privacy_level(String privacy) {
        ResponseEntity<List<Activity>> response = activityController.getActivities(privacy, loginResponse.getToken());
        System.out.println(response.getBody());
        assertEquals(1, response.getBody().size());
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

    @And("I am a {string} of this activity")
    public void iAmAOfThisActivity(String roleString) {
        ActivityMembership.Role role = ActivityMembership.Role.valueOf(roleString);
        ActivityMembership membership = new ActivityMembership(activity, profile, role);
        membershipRepository.save(membership);
    }

    @When("I choose to change my role to {string}")
    public void iChooseToChangeMyRoleTo(String roleString) {
        ActivityRoleUpdateRequest updateRequest = new ActivityRoleUpdateRequest();
        updateRequest.setRole(roleString);
        responseEntity = activityController.changeProfilesActivityRole(updateRequest, loginResponse.getToken(), jwtUtil.extractId(loginResponse.getToken()), activityRepository.getLastInsertedId());
    }

    @Then("I am now a {string} of the activity")
    public void iAmNowAOfTheActivity(String roleString) {
        ActivityMembership.Role role = ActivityMembership.Role.valueOf(roleString);
        Optional<ActivityMembership> optionalActivityMembership = membershipRepository.findByActivity_IdAndProfile_Id(activity.getId(), profile.getId());
        ActivityMembership activityMembership = optionalActivityMembership.get();
        assertEquals(role, activityMembership.getRole());
    }

    @Given("I create the following activities, making them public")
    public void i_create_the_following_activities_making_them_public(io.cucumber.datatable.DataTable activityNames) {
        typeRepository.save(new ActivityType("Running"));
        for (String activityName: activityNames.asList()) {
            assertEquals(201, activityController.createActivity(jwtUtil.extractId(loginResponse.getToken()), activity = createNormalActivity(activityName, "Christchurch"), loginResponse.getToken()).getStatusCodeValue());
            assertEquals(200, activityController.editActivityPrivacy(new PrivacyRequest("public"), loginResponse.getToken(), loginResponse.getUserId(), activityRepository.getLastInsertedId()).getStatusCodeValue());
        }
    }

    @Given("I create the following activities, making them public and the account with email {string} an organiser of each")
    public void i_create_the_following_activities_making_them_public_and_the_account_with_email_an_organiser_of_each(String email, io.cucumber.datatable.DataTable activityNames) {
        Long profileId = profileRepository.findByEmail(email).get(0).getId();
        for (String activityName: activityNames.asList()) {
            assertEquals(201, activityController.createActivity(jwtUtil.extractId(loginResponse.getToken()), activity = createNormalActivity(activityName, "Christchurch"), loginResponse.getToken()).getStatusCodeValue());
            assertEquals(200, activityController.editActivityPrivacy(new PrivacyRequest("public"), loginResponse.getToken(), loginResponse.getUserId(), activityRepository.getLastInsertedId()).getStatusCodeValue());
            assertEquals(201, activityController.addActivityRole(loginResponse.getToken(), profileId, activityRepository.getLastInsertedId(), "organiser").getStatusCodeValue());
        }
    }

    @When("I go to view the activities that I am a creator or organiser of.")
    public void i_go_to_view_the_activities_that_I_am_a_creator_or_organiser_of() {
        ResponseEntity<SimplifiedActivitiesResponse> response = activityController.getUsersActivitiesByRole(loginResponse.getToken(), loginResponse.getUserId(), 5, 0, "creatorOrOrganiser");
        assertEquals(200, response.getStatusCodeValue());
        simplifiedActivitiesResponse = response.getBody();
    }

    @And("I go to discover new public activities.")
    public void i_go_to_discover_new_public_activities() {
        ResponseEntity<SimplifiedActivitiesResponse> response = activityController.getUsersActivitiesByRole(loginResponse.getToken(), loginResponse.getUserId(), 5, 0, "discover");
        assertEquals(200, response.getStatusCodeValue());
        simplifiedActivitiesResponse = response.getBody();
    }

    @Then("Four activities are returned.")
    public void four_activities_are_returned() {
        assertEquals(4, simplifiedActivitiesResponse.getResults().size());
    }


    @When("I share the activity with email {string}, and give them the role {string}.")
    public void shareActivity(String email, String roleString) {
        List<MembersRequest> membersRequests = new ArrayList<>();
        membersRequests.add(new MembersRequest(email, roleString));
        ActivityMembership.Role role = ActivityMembership.Role.valueOf(roleString.toUpperCase());
        PrivacyRequest privacyRequest = new PrivacyRequest("friends", membersRequests);
        assertEquals(200, activityController.editActivityPrivacy(privacyRequest, loginResponse.getToken(), loginResponse.getUserId(), activityRepository.getLastInsertedId()).getStatusCodeValue());
    }

    @Then("The activity now has one creator and one follower.")
    public void checkActivityMembers() {
        assertEquals(1, membershipRepository.findActivityMembershipsByRole(ActivityMembership.Role.CREATOR).size());
        assertEquals(1, membershipRepository.findActivityMembershipsByRole(ActivityMembership.Role.FOLLOWER).size());
    }

    @When("I change the privacy level to friends.")
    public void i_change_the_privacy_level_to_friends() {
        assertEquals(200, activityController.editActivityPrivacy(new PrivacyRequest("friends"), loginResponse.getToken(), loginResponse.getUserId(), activityRepository.getLastInsertedId()).getStatusCodeValue());
    }

    @Then("The activity privacy level is now {int}.")
    public void the_activity_privacy_level_is_now(Integer level) {
        List<Activity> activities = activityRepository.findAll();
        assertEquals(level, activities.get(0).getPrivacyLevel());
    }


    @When("I want to see who is following my activity")
    public void iWantToSeeWhoIsFollowingMyActivity() {
        actualMemberProfileResponse = activityController.getActivityMembers(loginResponse.getToken(), activity.getId());
    }

    @Then("The ID first name last name and role of All people with roles in the activity is returned")
    public void theIDFirstNameLastNameAndRoleOfAllPeopleWithRolesInTheActivityIsReturned() {
        assertEquals(expectedMemberProfileResponse, activityController.getActivityMembers(loginResponse.getToken(), activity.getId()));
    }

    @And("an activity exists in the database with {int} participants, {int} followers and {int} organizers")
    public void anActivityExistsInTheDatabaseWithParticipantsFollowersAndOrganizers(int numParticipants, int numFollowers, int numOrganizers) {
        List<ActivityMembership.Role> roles = Arrays.asList(ActivityMembership.Role.PARTICIPANT, ActivityMembership.Role.FOLLOWER, ActivityMembership.Role.ORGANISER);
        int[] numRoles = {numParticipants, numFollowers, numOrganizers};
        activity = createNormalActivity("Cool activity", "Christchurch");
        activityRepository.save(activity);
        List<ActivityMemberProfileResponse> activityMemberProfileResponseList = new ArrayList<>();
        for(int i = 0; i < roles.size(); i++){
            for(int j = 0; j < numRoles[i]; j++){
                Profile newProfile = createNormalProfile("email"+j+i, "password");
                profileRepository.save(newProfile);
                emailRepository.save(new Email("email"+j+i, true, newProfile));
                membershipRepository.save(new ActivityMembership(activity, newProfile, roles.get(i)));
                activityMemberProfileResponseList.add(new ActivityMemberProfileResponse(newProfile.getId(), newProfile.getFirstname(), newProfile.getLastname(), newProfile.getPrimary_email(), roles.get(i)));
            }
        }
        expectedMemberProfileResponse = new ResponseEntity<>(activityMemberProfileResponseList, HttpStatus.OK);
    }
}
