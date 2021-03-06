package com.springvuegradle.service;

import com.springvuegradle.controller.Profile_Controller;
import com.springvuegradle.enums.AuthLevel;
import com.springvuegradle.enums.ProfileErrorMessage;
import com.springvuegradle.model.Email;
import com.springvuegradle.model.Profile;
import com.springvuegradle.model.ProfileLocation;
import com.springvuegradle.model.ProfileSearchCriteria;
import com.springvuegradle.utilities.ProfileTestUtils;
import com.springvuegradle.repositories.EmailRepository;
import com.springvuegradle.repositories.ProfileRepository;
import com.springvuegradle.utilities.ProfileLocationTestUtils;
import org.junit.jupiter.api.BeforeEach;
import com.springvuegradle.repositories.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class ProfileServiceTest {

    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    private ProfileService testService;
    @Autowired
    private Profile_Controller controller;

    @Autowired
    ProfileService profileService;

    @Autowired
    ProfileLocationRepository profileLocationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @AfterEach
    void tearDown() {
        profileLocationRepository.deleteAll();
    }

    private Profile jimmyOne, jimmyTwo, steven, maurice, nicknamedQuick;
    private List<Profile> profilesWithSameSurnameAsJimmy;


    @BeforeEach
    public void setUp(){
        emailRepository.deleteAll();
        profileRepository.deleteAll();
        jimmyOne = ProfileTestUtils.createProfileJimmy();
        jimmyOne.setPassports(new HashSet<>());
        jimmyTwo = ProfileTestUtils.createProfileJimmyAlternate();
        nicknamedQuick = ProfileTestUtils.createProfileNicknameMatchesJimmySurname();
        steven = ProfileTestUtils.createProfileWithMinimalFields();
        maurice = ProfileTestUtils.createNormalProfileMaurice();
        maurice.setPassports(new HashSet<>());
        profilesWithSameSurnameAsJimmy = ProfileTestUtils.createProfilesWithSameSurnameAsJimmy();
    }

    @Test
    void getUsersWithNoCriteriaReturnsAllUsersTest() {
        jimmyOne = profileRepository.save(jimmyOne);
        nicknamedQuick = profileRepository.save(nicknamedQuick);
        maurice = profileRepository.save(maurice);
        steven = profileRepository.save(steven);

        Set<Profile> expectedProfiles = new HashSet<>();
        expectedProfiles.add(jimmyOne);
        expectedProfiles.add(nicknamedQuick);
        expectedProfiles.add(maurice);
        expectedProfiles.add(steven);

        PageRequest request = PageRequest.of(0, (int) profileRepository.count());
        Page<Profile> result = testService.getUsers(new ProfileSearchCriteria(), request);
        assertTrue(result.getContent().containsAll(expectedProfiles), "Check no duplicates in result page");
        assertEquals(expectedProfiles.size(), result.getSize());
    }

    @Test
    void getUsersByFirstNameNormalTest() {
        jimmyOne = profileRepository.save(jimmyOne);
        jimmyTwo = profileRepository.save(jimmyTwo);
        nicknamedQuick = profileRepository.save(nicknamedQuick);
        maurice = profileRepository.save(maurice);
        profilesWithSameSurnameAsJimmy = profileRepository.saveAll(profilesWithSameSurnameAsJimmy);

        Set<Profile> expectedProfiles = new HashSet<>();
        expectedProfiles.add(jimmyOne);
        expectedProfiles.add(jimmyTwo);

        PageRequest request = PageRequest.of(0, (int) profileRepository.count());
        ProfileSearchCriteria criteria = new ProfileSearchCriteria();
        criteria.setAnyName(jimmyOne.getFirstname());
        Page<Profile> actualProfiles = testService.getUsers(criteria, request);
        assertTrue(expectedProfiles.containsAll(actualProfiles.getContent()), "Check page contains the correct profiles.");
        assertEquals(2, actualProfiles.getTotalElements(), "Check page is of the right size.");
    }

    @Test
    void getUsersByMiddleNameNormalTest() {
        jimmyOne = profileRepository.save(jimmyOne);
        jimmyTwo = profileRepository.save(jimmyTwo);
        nicknamedQuick = profileRepository.save(nicknamedQuick);
        maurice = profileRepository.save(maurice);
        steven = profileRepository.save(steven);
        profilesWithSameSurnameAsJimmy = profileRepository.saveAll(profilesWithSameSurnameAsJimmy);

        Set<Profile> expectedProfiles = new HashSet<>();
        expectedProfiles.add(maurice);

        PageRequest request = PageRequest.of(0, (int) profileRepository.count());
        ProfileSearchCriteria criteria = new ProfileSearchCriteria();
        criteria.setAnyName(maurice.getMiddlename());
        Page<Profile> actualProfiles = testService.getUsers(criteria, request);

        assertTrue(expectedProfiles.containsAll(actualProfiles.getContent()), "Check page contains the correct profiles.");
        assertEquals(expectedProfiles.size(), actualProfiles.getTotalElements(), "Check page is of the right size.");
    }

    @Test
    void getUsersByLastNameNormalTest() {
        jimmyOne = profileRepository.save(jimmyOne);
        maurice = profileRepository.save(maurice);
        steven = profileRepository.save(steven);
        profilesWithSameSurnameAsJimmy = profileRepository.saveAll(profilesWithSameSurnameAsJimmy);

        Set<Profile> expectedProfiles = new HashSet<>();
        expectedProfiles.add(jimmyOne);
        expectedProfiles.addAll(profilesWithSameSurnameAsJimmy);
        ProfileSearchCriteria criteria = new ProfileSearchCriteria(null, null, null,
                null, null);
        criteria.setAnyName(jimmyOne.getLastname());
        criteria.setWholeProfileNameSearch(false);

        PageRequest request = PageRequest.of(0, Math.toIntExact(profileRepository.count()));

        Page<Profile> actualProfiles = testService.getUsers(criteria, request);
        assertTrue(expectedProfiles.containsAll(actualProfiles.getContent()), "Check page contains the correct profiles.");
        assertEquals(expectedProfiles.size(), actualProfiles.getTotalElements(), "Check page is of the right size.");
    }

    @Test
    void getUsersByFullNameNormalTest() {
        jimmyOne = profileRepository.save(jimmyOne);
        jimmyTwo = profileRepository.save(jimmyTwo);
        steven = profileRepository.save(steven);
        profilesWithSameSurnameAsJimmy = profileRepository.saveAll(profilesWithSameSurnameAsJimmy);

        Set<Profile> expectedProfiles = new HashSet<>();
        expectedProfiles.add(jimmyOne);
        expectedProfiles.add(jimmyTwo);

        PageRequest request = PageRequest.of(0, Math.toIntExact(profileRepository.count()));

        ProfileSearchCriteria criteria = new ProfileSearchCriteria(jimmyOne.getFirstname(), jimmyOne.getMiddlename(),
                jimmyOne.getLastname(), jimmyOne.getNickname(), null);
        criteria.setWholeProfileNameSearch(true);
        Page<Profile> actualProfiles = testService.getUsers(criteria, request);
        assertTrue(expectedProfiles.containsAll(actualProfiles.getContent()), "Check page contains the correct profiles.");
        assertEquals(expectedProfiles.size(), actualProfiles.getTotalElements(), "Check page is of the right size.");
    }

    @Test
    void getUsersByNicknameNormalTest() {
        nicknamedQuick = profileRepository.save(nicknamedQuick);

        Set<Profile> expectedProfiles = new HashSet<>();
        expectedProfiles.add(nicknamedQuick);

        PageRequest request = PageRequest.of(0, Math.toIntExact(profileRepository.count()));
        ProfileSearchCriteria criteria = new ProfileSearchCriteria();
        criteria.setAnyName(nicknamedQuick.getNickname());
        Page<Profile> actualProfiles = testService.getUsers(criteria, request);
        assertTrue(expectedProfiles.containsAll(actualProfiles.getContent()), "Check page contains the correct profiles.");
        assertEquals(expectedProfiles.size(), actualProfiles.getTotalElements(), "Check page is of the right size.");
    }

    @Test
    void getUsersByEmailNormalTest() {
        String email = steven.getPrimary_email();
        saveWithEmails(jimmyOne);
        saveWithEmails(jimmyTwo);
        steven = saveWithEmails(steven);

        Set<Profile> expectedProfiles = new HashSet<>();
        expectedProfiles.add(steven);

        PageRequest request = PageRequest.of(0, Math.toIntExact(profileRepository.count()));
        ProfileSearchCriteria criteria = new ProfileSearchCriteria(null, null, null,
                null, email);
        Page<Profile> actualProfiles = testService.getUsers(criteria, request);
        assertTrue(expectedProfiles.containsAll(actualProfiles.getContent()), "Check page contains the correct profiles.");
        assertEquals(expectedProfiles.size(), actualProfiles.getTotalElements(), "Check page is of the right size.");
    }

    @Test
    void getUsersByEmailWithUnusedEmailReturnsNothingTest() {
        saveWithEmails(jimmyOne);
        saveWithEmails(jimmyTwo);
        steven = saveWithEmails(steven);

        Set<Profile> expectedProfiles = new HashSet<>();
        expectedProfiles.add(steven);

        PageRequest request = PageRequest.of(0, Math.toIntExact(profileRepository.count()));
        ProfileSearchCriteria criteria = new ProfileSearchCriteria(null, null, null,
                null, "not a real email");
        Page<Profile> actualProfiles = testService.getUsers(criteria, request);
        assertTrue(actualProfiles.isEmpty(), "Check no results are returned");
    }

    @Test
    void getUsersWithNoProfilesMatchingParamsReturnsNoProfilesTest() {
        jimmyOne = profileRepository.save(jimmyOne);
        maurice = profileRepository.save(maurice);
        steven = profileRepository.save(steven);

        PageRequest request = PageRequest.of(0, Math.toIntExact(profileRepository.count()));
        Page<Profile> actualProfiles;
        ProfileSearchCriteria criteria = new ProfileSearchCriteria("Jim", "Bim", "Dim",
                "Gim", null);
        actualProfiles = testService.getUsers(criteria, request);
        assertTrue(actualProfiles.isEmpty());
    }

    @Test
    void getUsersMatchingIsNotCaseSensitiveTest() {
        jimmyTwo = profileRepository.save(jimmyTwo);
        nicknamedQuick = profileRepository.save(nicknamedQuick);
        steven = profileRepository.save(steven);

        Set<Profile> expectedProfiles = new HashSet<>();
        expectedProfiles.add(steven);

        PageRequest request = PageRequest.of(0, Math.toIntExact(profileRepository.count()));
        ProfileSearchCriteria criteria = new ProfileSearchCriteria("ste", null, null,
                null, null);
        Page<Profile> actualProfiles = testService.getUsers(criteria, request);
        assertTrue(expectedProfiles.containsAll(actualProfiles.getContent()), "Check page contains the correct profiles.");
        assertEquals(expectedProfiles.size(), actualProfiles.getTotalElements(), "Check page is of the right size.");
    }

    /**
     * Test to ensure HTTP Ok response returned when successfully adding a location to a profile.
     **/
    @Test
    void testAddLocationResponse() {
        ProfileLocation location = ProfileLocationTestUtils.createValidProfileLocation();
        Profile profile = createProfile();
        profileRepository.save(profile);
        ResponseEntity<String> response = profileService.updateProfileLocation(location, profile.getId());
        assertEquals(response, new ResponseEntity<>(HttpStatus.OK));
    }

    /**
     * Test to ensure data for profiles location matches the provided json data
     **/
    @Test
    void testAddLocationData() {
        ProfileLocation location = ProfileLocationTestUtils.createValidProfileLocation();
        Profile profile = createProfile();
        profileRepository.save(profile);
        profileService.updateProfileLocation(location, profile.getId());
        assertEquals(profile.getProfileLocation(), location);
    }

    /**
     * Test to ensure when changing the profiles location, the new location is now associated with the profile
     **/
    @Test
    void testChangeLocationData() {
        ProfileLocation location = ProfileLocationTestUtils.createValidProfileLocation();
        Profile profile = createProfile();
        profile.setLocation(location);
        profileRepository.save(profile);
        ProfileLocation newLocation = ProfileLocationTestUtils.createUpdatedProfileLocation();
        profileService.updateProfileLocation(newLocation, profile.getId());
        assertEquals(profile.getProfileLocation(), newLocation);
    }

    /**
     * Tests to ensure when locations update there latitude and longitude, the service method updates it correctly
     * in the database.
     */
    @Test
    void testChangeLocationCoordinates() {
        ProfileLocation location = ProfileLocationTestUtils.createValidProfileLocation();
        ProfileLocation newLocation = ProfileLocationTestUtils.createUpdatedProfileLocation();
        Profile profile = createProfile();
        profile.setLocation(location);
        ArrayList<Double> currentCoordinates = new ArrayList<>();
        ArrayList<Double> newCoordinates = new ArrayList<>();

        profileRepository.save(profile);

        double currentLatitude = profile.getProfileLocation().getLatitude();
        double currentLongitude = profile.getProfileLocation().getLongitude();
        currentCoordinates.add(currentLatitude);
        currentCoordinates.add(currentLongitude);

        profileService.updateProfileLocation(newLocation, profile.getId());
        double newLatitude = profile.getProfileLocation().getLatitude();
        double newLongitude = profile.getProfileLocation().getLongitude();
        newCoordinates.add(newLatitude);
        newCoordinates.add(newLongitude);

        assertNotEquals(currentCoordinates, newCoordinates);
    }

    /**
     * Test to ensure when an invalid profile ID is given a 404 status will be returned
     **/
    @Test
    void testNonExistentProfileIdStatus(){
        ProfileLocation location = ProfileLocationTestUtils.createValidProfileLocation();
        ResponseEntity<String> response = profileService.updateProfileLocation(location,-1L);
        assertEquals(response, new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Test to ensure when an invalid profile ID is given the location is not added to the profile_location table
     **/
    @Test
    void testNonExistentProfileData(){
        ProfileLocation location = ProfileLocationTestUtils.createValidProfileLocation();
        ResponseEntity<String> response = profileService.updateProfileLocation(location, -1L);
        assertEquals(0L, profileLocationRepository.count());
    }

    @Test
    void setUserAuthLevelAsAdminTest(){
        steven.setAuthLevel(5);
        steven = profileRepository.save(steven);
        long id = steven.getId();
        assertNotEquals(1, steven.getAuthLevel(), "Sanity check: Profile is not an admin");
        testService.setUserAuthLevel(id, AuthLevel.ADMIN);
        Optional<Profile> updated =  profileRepository.findById(id);
        if (updated.isEmpty()) {
            fail("Error: Updated profile does not exist in repository");
        } else {
            assertEquals(1, updated.get().getAuthLevel());
        }
    }

    @Test
    void setUserAuthLevelAsNormalTest(){
        steven.setAuthLevel(1);
        steven = profileRepository.save(steven);
        long id = steven.getId();
        assertEquals(1, steven.getAuthLevel(), "Sanity check: Profile is an admin");
        testService.setUserAuthLevel(id, AuthLevel.USER);
        Optional<Profile> updated =  profileRepository.findById(id);
        if (updated.isEmpty()) {
            fail("Error: Updated profile does not exist in repository");
        } else {
            assertEquals(5, updated.get().getAuthLevel());
        }
    }

    @Test
    void setUserAuthLevelWithTooLowAuthLevelTest(){
        steven.setAuthLevel(1);
        steven = profileRepository.save(steven);
        long id = steven.getId();
        try {
            testService.setUserAuthLevel(id, AuthLevel.DEFAULT_ADMIN);
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(ProfileErrorMessage.INVALID_AUTH_LEVEL.getMessage(), e.getMessage());
        } catch (Exception e) {
            fail("Should have thrown an IllegalArgumentException");
        }
    }

    @Test
    void setUserAuthLevelWhereUserDoesNotExistTest(){
        long badId = 0;
        assertFalse(profileRepository.existsById(badId));
        try {
            testService.setUserAuthLevel(badId, AuthLevel.USER);
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(ProfileErrorMessage.PROFILE_NOT_FOUND.getMessage(), e.getMessage());
        } catch (Exception e) {
            fail("Should have thrown an IllegalArgumentException");
        }
    }

    @Test
    void checkValidEmailExistsInDatabaseTest(){
        saveWithEmails(steven);
        String email = steven.getPrimary_email();
        assertTrue(testService.checkEmailExistsInDB(email));
    }

    @Test
    void checkInvalidEmailExistsInDatabaseTest(){
        String email = "doesNotExistInDatabase@gmail.com";
        assertFalse(testService.checkEmailExistsInDB(email));
    }

    @Test
    void deleteProfileSimpleTest() {
        saveWithEmails(steven);
        profileService.deleteProfile(steven.getId());
    }

    /**
     * Example test profile to use in tests
     **/
    public Profile createProfile(){
        return new Profile(null, "Maurice", "Benson", "Jack", "Jacky", "jacky@google.com", new String[]{"additionaldoda@email.com"}, "jacky'sSecuredPwd",
                "Jacky loves to ride his bike on crazy mountains.", new GregorianCalendar(1985, Calendar.DECEMBER,
                20), "male", 1, new String[]{}, new String[]{});
    }

    private Profile saveWithEmails(Profile profile) {
        Profile updated = profileRepository.save(profile);
        for (Email email: profile.retrieveEmails()) {
            emailRepository.save(email);
        }
        return  updated;
    }

}