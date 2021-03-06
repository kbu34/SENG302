package com.springvuegradle.controller;

import com.springvuegradle.dto.*;
import com.springvuegradle.enums.ActivityMessage;
import com.springvuegradle.dto.responses.ActivityMemberRoleResponse;
import com.springvuegradle.dto.responses.ProfileSummary;
import com.springvuegradle.enums.ActivityResponseMessage;
import com.springvuegradle.enums.AuthenticationErrorMessage;
import com.springvuegradle.enums.ProfileErrorMessage;
import com.springvuegradle.model.*;
import com.springvuegradle.config.MockServiceConfig;
import com.springvuegradle.dto.SimplifiedActivitiesResponse;
import com.springvuegradle.enums.ActivityPrivacy;
import com.springvuegradle.model.Activity;
import com.springvuegradle.service.ActivitySearchService;
import com.springvuegradle.utilities.ActivityTestUtils;
import com.springvuegradle.repositories.ActivityRepository;
import com.springvuegradle.service.ActivityService;
import com.springvuegradle.service.SecurityService;
import com.springvuegradle.utilities.FormatHelper;
import com.springvuegradle.utilities.JwtUtil;
import com.springvuegradle.utilities.ProfileTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("mock-service")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {MockServiceConfig.class})
class ActivityControllerMockedTest {

    @Autowired
    ActivityService mockService;
    @Autowired
    ActivitySearchService mockActivitySearchService;
    @Autowired
    ActivityRepository activityRepository;
    @Autowired
    JwtUtil mockJwt;
    @Autowired
    ActivityController activityController;
    @Autowired
    ActivitySearchController activitySearchController;
    @Autowired
    ActivityRepository mockRepo;
    @Autowired
    SecurityService mockSecurity;

    @AfterEach
    private void tearDown() {
        mockRepo.deleteAll();
        Mockito.reset(mockService);
        Mockito.reset(mockActivitySearchService);
        Mockito.reset(mockJwt);
        Mockito.reset(mockSecurity);
    }


    @Test
    void removeMembershipFromActivitySuccessTest() {
        long mockActivityId = 10;
        long mockProfileId = 11;
        long editingUserId = 11;
        String mockToken = "token";
        Mockito.when(mockJwt.extractId(mockToken)).thenReturn(editingUserId);
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.doNothing().when(mockService).removeUserRoleFromActivity(editingUserId, mockProfileId, mockActivityId);
        ResponseEntity<String> actualResponse = activityController.deleteActivityMembership(mockToken, mockProfileId, mockActivityId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void removeMembershipFromActivityFailTest() {
        long mockActivityId = 10;
        long mockProfileId = 11;
        long editingUserId = 11;
        String mockToken = "token";
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockJwt.extractId(mockToken)).thenReturn(editingUserId);
        Mockito.doThrow(NoSuchElementException.class).when(mockService).removeUserRoleFromActivity(mockProfileId, mockProfileId, mockActivityId);
        ResponseEntity<String> actualResponse = activityController.deleteActivityMembership(mockToken, mockProfileId, mockActivityId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getActivitiesWithPrivacyLevelSuccessTest() {
        String mockToken = "token";
        Activity mockActivity = ActivityTestUtils.createNormalActivity();
        List<Activity> activityList = new ArrayList<>();
        activityList.add(mockActivity);
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockService.getActivitiesWithPrivacyLevel(ActivityPrivacy.PUBLIC)).thenReturn(activityList);
        ResponseEntity<List<Activity>> actualResponse = activityController.getActivities("public", mockToken);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getActivityWithPrivacyLevelFailTest() {
        String mockToken = "token";
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        ResponseEntity<List<Activity>> actualResponse = activityController.getActivities(mockToken, "fail");
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getActivityByValidActivityIdTest() {
        long mockActivityId = 10;
        String mockToken = "token";
        long mockProfileId = 25;
        int mockAuthLevel = 5;
        Activity mockActivity = ActivityTestUtils.createNormalActivity();
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.OK);

        mockRepo.save(mockActivity);
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockJwt.extractId(mockToken)).thenReturn(mockProfileId);
        Mockito.when(mockJwt.extractPermission(mockToken)).thenReturn(mockAuthLevel);
        Mockito.when(mockService.getActivityByActivityId(mockProfileId, mockActivityId, mockAuthLevel)).thenReturn(mockActivity);
        ResponseEntity<Activity> actualResponse = activityController.getActivity(mockToken, mockActivityId);

        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getActivityByInvalidIdTest() {
        long mockActivityId = 10;
        String mockToken = "token";
        long mockProfileId = 25;
        int mockAuthLevel = 5;
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockJwt.extractPermission(mockToken)).thenReturn(mockAuthLevel);
        Mockito.when(mockService.getActivityByActivityId(mockProfileId, mockActivityId, mockAuthLevel)).thenReturn(null);
        ResponseEntity<Activity> actualResponse = activityController.getActivity(mockToken, mockActivityId);

        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getActivityByIdInvalidTokenTest(){
        long mockActivityId = 10;
        String mockToken = "invalid token";
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.FORBIDDEN);

        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(false);
        ResponseEntity<Activity> actualResponse = activityController.getActivity(mockToken, mockActivityId);

        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getActivityByIdNoTokenTest() {
        long mockActivityId = 10;
        long mockProfileId = 25;
        ResponseEntity<String> expectedResponse = new ResponseEntity<>((HttpStatus.UNAUTHORIZED));
        ResponseEntity<Activity> actualResponse = activityController.getActivity(null, mockActivityId);

        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getActivityMembersNormalTest() {
        String roleName = "participant";
        int count = 5;
        int startIndex = 0;

        long mockId = 10;
        Pageable mockPageRequest = PageRequest.of(0, 5);
        String mockToken = "token";
        Page<Profile> mockPage = new PageImpl<>(ProfileTestUtils.createProfilesWithSameSurnameAsJimmy(), mockPageRequest, 12);
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockService.getActivityMembersByRole(mockId, ActivityMembership.Role.PARTICIPANT, mockPageRequest))
                .thenReturn(mockPage);
        List<ProfileSummary> summaries = FormatHelper.createProfileSummaries(mockPage.getContent());
        ActivityMemberRoleResponse expectedBody = new ActivityMemberRoleResponse(summaries);
        ResponseEntity<ActivityMemberRoleResponse> expectedResponse = new ResponseEntity<>(expectedBody, HttpStatus.OK);
        assertEquals(expectedResponse, activityController.getActivityMembers(mockToken, mockId, roleName, count, startIndex));
    }

    @Test
    void getActivityMembersWithInvalidTokenTest() {
        String roleName = "participant";
        int count = 5;
        int startIndex = 0;
        long mockId = 10;
        String mockToken = "token";
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(false);
        ActivityMemberRoleResponse expectedBody = new ActivityMemberRoleResponse(AuthenticationErrorMessage.INVALID_CREDENTIALS);
        ResponseEntity<ActivityMemberRoleResponse> expectedResponse = new ResponseEntity<>(expectedBody, HttpStatus.UNAUTHORIZED);
        assertEquals(expectedResponse, activityController.getActivityMembers(mockToken, mockId, roleName, count, startIndex));
    }

    @Test
    void getActivityMembersWithSomePagingParametersMissingTest() {
        String roleName = "participant";
        int count = 5;

        long mockId = 10;
        String mockToken = "token";
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        ActivityMemberRoleResponse expectedBody = new ActivityMemberRoleResponse(ProfileErrorMessage.INVALID_SEARCH_COUNT);
        ResponseEntity<ActivityMemberRoleResponse> expectedResponse = new ResponseEntity<>(expectedBody, HttpStatus.BAD_REQUEST);
        assertEquals(expectedResponse, activityController.getActivityMembers(mockToken, mockId, roleName, count, null));
    }

    @Test
    void getActivityMembersWithServiceThrowingNotFoundErrorTest() {
        String roleName = "participant";
        int count = 5;
        int startIndex = 0;

        long mockId = 10;
        Pageable mockPageRequest = PageRequest.of(0, 5);
        String mockToken = "token";
        Exception serviceException = new IllegalArgumentException(ActivityResponseMessage.INVALID_ACTIVITY.toString());
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockService.getActivityMembersByRole(mockId, ActivityMembership.Role.PARTICIPANT, mockPageRequest))
                .thenThrow(serviceException);
        ActivityMemberRoleResponse expectedBody = new ActivityMemberRoleResponse(ActivityResponseMessage.INVALID_ACTIVITY);
        ResponseEntity<ActivityMemberRoleResponse> expectedResponse = new ResponseEntity<>(expectedBody, HttpStatus.NOT_FOUND);
        assertEquals(expectedResponse, activityController.getActivityMembers(mockToken, mockId, roleName, count, startIndex));
    }

    @Test
    void getActivityMembersWithInvalidRoleNameTest() {
        String roleName = "iohfad";
        int count = 5;
        int startIndex = 0;
        long mockId = 10;
        String mockToken = "token";
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        ResponseEntity<ActivityMemberRoleResponse> actualResponse = activityController.getActivityMembers(mockToken, mockId, roleName, count, startIndex);
        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    void getUsersActivitiesSuccessTest() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        String mockToken = "bob";
        long mockProfileId = 420;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        ResponseEntity<SimplifiedActivitiesResponse> actualResponse = activityController.getUsersActivitiesByRole(mockToken, mockProfileId, 5, 0, "creator");
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getUsersActivitiesInvalidCountTest() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        String mockToken = "bob";
        long mockProfileId = 420;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        ResponseEntity<SimplifiedActivitiesResponse> actualResponse = activityController.getUsersActivitiesByRole(mockToken, mockProfileId, 0, 0, "creator");
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void postActivityParticipationSuccessTest() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.CREATED);
        String mockToken = "bob";
        ActivityParticipationRequest mockParticipationRequest = ActivityTestUtils.createNormalParticipationRequest();
        long mockProfileId = 420;
        long mockActivityId = 505;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        ResponseEntity<String> actualResponse = activityController.addActivityParticipation(mockParticipationRequest, mockToken, mockProfileId, mockActivityId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void postActivityParticipationWhenServiceThrowsExceptionReturnsA403ErrorTest() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        String mockToken = "bob";
        ActivityParticipationRequest mockParticipationRequest = ActivityTestUtils.createNormalParticipationRequest();
        ActivityParticipation mockParticipation = ActivityTestUtils.createNormalParticipation();
        long mockProfileId = 420;
        long mockActivityId = 505;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.doThrow(new IllegalArgumentException()).when(mockService).createParticipation(mockActivityId, mockProfileId, mockParticipation);
        ResponseEntity<String> actualResponse = activityController.addActivityParticipation(mockParticipationRequest, mockToken, mockProfileId, mockActivityId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void editActivityParticipationSuccessTest() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        String mockToken = ":)";
        ActivityParticipationRequest updatedParticipation = ActivityTestUtils.createADifferentParticipationRequest();
        long mockParticipationId = 555;
        long mockProfileId = 420;
        long mockActivityId = 505;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        ResponseEntity<String> actualResponse = activityController.updateParticipation(updatedParticipation, mockToken, mockProfileId, mockActivityId, mockParticipationId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void editActivityParticipationThrow403Test() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        String mockToken = ":)";
        ActivityParticipationRequest mockUpdatedParticipationRequest = ActivityTestUtils.createADifferentParticipationRequest();
        ActivityParticipation mockUpdatedParticipation = ActivityTestUtils.createADifferentParticipation();
        long mockParticipationId = 555;
        long mockProfileId = 420;
        long mockActivityId = 505;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.doThrow(new IllegalArgumentException()).when(mockService).editParticipation(mockActivityId, mockProfileId, mockParticipationId, mockUpdatedParticipation);
        ResponseEntity<String> actualResponse = activityController.updateParticipation(mockUpdatedParticipationRequest, mockToken, mockProfileId, mockActivityId, mockParticipationId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void deleteActivityParticipationSuccessTest() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        String mockToken = ":)";
        long mockParticipationId = 555;
        long mockProfileId = 420;
        long mockActivityId = 505;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockSecurity.checkEditPermission(mockToken, mockProfileId)).thenReturn(true);
        Mockito.when(mockService.removeParticipation(mockActivityId, mockProfileId, mockParticipationId)).thenReturn(true);
        ResponseEntity<String> actualResponse = activityController.deleteParticipation(mockToken, mockProfileId, mockActivityId, mockParticipationId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void deleteActivityParticipationFailThrow404Test() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        String mockToken = ":)";
        long mockParticipationId = 555;
        long mockProfileId = 420;
        long mockActivityId = 505;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockSecurity.checkEditPermission(mockToken, mockProfileId)).thenReturn(true);
        Mockito.when(mockService.removeParticipation(mockActivityId, mockProfileId, mockParticipationId)).thenReturn(false);
        ResponseEntity<String> actualResponse = activityController.deleteParticipation(mockToken, mockProfileId, mockActivityId, mockParticipationId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getActivityParticipationSuccessTest() {
        String mockToken = ":)";
        ActivityParticipation mockParticipation = new ActivityParticipation("The final score was 2 - 1.", "University Wins", "2020-02-20T08:00:00+1300",
                "2020-02-20T10:15:00+1300");

        ResponseEntity<ActivityParticipationResponse> expectedResponse = new ResponseEntity<>(new ActivityParticipationResponse(mockParticipation), HttpStatus.OK);
        long mockParticipationId = 555;
        long mockProfileId = 420;
        long mockActivityId = 505;
        Mockito.when(mockService.readParticipation(mockParticipationId)).thenReturn(mockParticipation);
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        ResponseEntity<ActivityParticipationResponse> actualResponse = activityController.getParticipation(mockToken, mockProfileId, mockActivityId, mockParticipationId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getActivityParticipationWhereDoesntExistTest() {
        String mockToken = ":)";
        ResponseEntity<ActivityParticipationResponse> expectedResponse = new ResponseEntity<>(new ActivityParticipationResponse(ActivityMessage.PARTICIPATION_NOT_FOUND.getMessage()), HttpStatus.NOT_FOUND);
        long mockParticipationId = 555;
        long mockProfileId = 420;
        long mockActivityId = 505;
        Mockito.when(mockService.readParticipation(mockParticipationId)).thenThrow(new IllegalArgumentException(ActivityMessage.PARTICIPATION_NOT_FOUND.getMessage()));
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        ResponseEntity<ActivityParticipationResponse> actualResponse = activityController.getParticipation(mockToken, mockProfileId, mockActivityId, mockParticipationId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getActivityParticipationSummariesValidTest() {
        String mockToken = "token";
        long mockActivityId = 10;
        List<ActivityParticipation> mockParticipationsList = ActivityTestUtils.createValidActivityParticipationsList();
        ResponseEntity<ActivityParticipationSummariesResponse> expectedResponse = new ResponseEntity<>(new ActivityParticipationSummariesResponse(mockParticipationsList), HttpStatus.OK);
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockService.readParticipationsFromActivity(mockActivityId)).thenReturn(mockParticipationsList);
        ResponseEntity<ActivityParticipationSummariesResponse> actualResponse = activityController.getParticipationSummaries(mockToken, mockActivityId);
        assertEquals(actualResponse.getBody().getAllActivityParticipation(), expectedResponse.getBody().getAllActivityParticipation());
    }

    @Test
    void getActivityParticipationSummariesInvalidTokenTest() {
        String mockToken = "invalidToken";
        long mockActivityId = 10;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(false);
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        ResponseEntity<ActivityParticipationSummariesResponse> actualResponse = activityController.getParticipationSummaries(mockToken, mockActivityId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getActivityParticipationSummariesNoTokenTest() {
        long mockActivityId = 10;
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        ResponseEntity<ActivityParticipationSummariesResponse> actualResponse = activityController.getParticipationSummaries(null, mockActivityId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void getActivityParticipationSummariesInvalidActivityIdTest() {
        long mockActivityId = 10;
        String mockToken = "token";
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockService.readParticipationsFromActivity(mockActivityId)).thenThrow(new IllegalArgumentException(ActivityResponseMessage.INVALID_ACTIVITY.toString()));
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        ResponseEntity<ActivityParticipationSummariesResponse> actualResponse = activityController.getParticipationSummaries(mockToken, mockActivityId);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }



    @Test
    void clearRoleOfActivitySuccessTest(){
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        String mockToken = "54321";
        long mockActivityID = 666;
        long mockProfileID = 123;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockJwt.extractPermission(mockToken)).thenReturn(0);
        Mockito.when(mockJwt.extractId(mockToken)).thenReturn(mockProfileID);
        Mockito.when(mockService.isProfileActivityCreator(mockProfileID, mockActivityID)).thenReturn(true);
        ResponseEntity actualResponse = activityController.clearRoleOfActivity(mockToken, mockActivityID, "participant");
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void clearRoleOfActivityNotOwnerTest(){
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        String mockToken = "54321";
        long mockActivityID = 666;
        long mockProfileID = 123;
        long mockPermission = 5;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockJwt.extractPermission(mockToken)).thenReturn(5);
        Mockito.when(mockSecurity.checkEditPermission(mockToken, mockActivityID)).thenReturn(false);
        Mockito.when(mockService.isProfileActivityCreator(mockProfileID, mockActivityID)).thenReturn(false);
        ResponseEntity actualResponse = activityController.clearRoleOfActivity(mockToken, mockActivityID, "participant");
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void clearRoleOfActivityNoTokenTest(){
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        String mockToken = null;
        long mockActivityID = 666;
        Mockito.when(mockJwt.extractId(mockToken)).thenReturn(54321l);
        Mockito.when(mockJwt.extractPermission(mockToken)).thenReturn(1);
        Mockito.when(mockSecurity.checkEditPermission(mockToken, mockActivityID)).thenReturn(true);
        Mockito.when(mockService.isProfileActivityCreator(54321l, mockActivityID)).thenReturn(true);
        ResponseEntity actualResponse = activityController.clearRoleOfActivity(mockToken, mockActivityID, "participant");
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void clearRoleOfActivityBadTokenTest(){
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        String mockToken = "badtoken";
        long mockActivityID = 666;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(false);
        Mockito.when(mockJwt.extractPermission(mockToken)).thenReturn(1);
        Mockito.when(mockJwt.extractId(mockToken)).thenReturn(54321L);
        Mockito.when(mockService.isProfileActivityCreator(54321L, mockActivityID)).thenReturn(true);
        ResponseEntity actualResponse = activityController.clearRoleOfActivity(mockToken, mockActivityID, "participant");
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void clearRoleOfActivityInvalidRoleTest(){
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        String mockToken = "54321";
        long mockActivityID = 666;
        long mockProfileID = 54321;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockJwt.extractPermission(mockToken)).thenReturn(0);
        Mockito.when(mockJwt.extractId(mockToken)).thenReturn(mockProfileID);
        Mockito.when(mockService.isProfileActivityCreator(mockProfileID, mockActivityID)).thenReturn(true);
        ResponseEntity actualResponse = activityController.clearRoleOfActivity(mockToken, mockActivityID, "partycrasher");
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    @Test
    void clearRoleOfActivityNothingtoDeleteTest(){
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        String mockToken = "54321";
        long mockActivityID = 666;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockJwt.extractId(mockToken)).thenReturn(54321l);
        Mockito.when(mockService.isProfileActivityCreator(54321l, mockActivityID)).thenReturn(true);
        Mockito.doThrow(new NoSuchElementException()).when(mockService).clearActivityRoleList(54321l, mockActivityID, "PARTICIPANT");
        ResponseEntity actualResponse = activityController.clearRoleOfActivity(mockToken, mockActivityID, "participant");
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    /**
     * Tests the getActivityRole method.
     */
    @Test
    void getActivityRoleTest() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        String mockToken = "54321";
        long mockProfileId = 987;
        long mockActivityID = 666;
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockSecurity.checkEditPermission(mockToken, mockProfileId)).thenReturn(true);
        ResponseEntity actualResponse = activityController.getActivityRole(mockToken, mockProfileId,mockActivityID);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
    }

    /**
     * Tests the getActivitiesByName method. When all data is as expected, we test it returns a correct response.
     */
    @Test
    void getActivitiesByNameReturnsNormalResponseTest(){
        long mockProfileId = 987;
        String searchMethod = "all";
        String mockActivityName = "Valid Activity Name";
        String mockToken = "validToken";
        int mockPermission = 2;
        int count = 2;
        int startIndex = 1;
        List<Activity> expectedActivities = new ArrayList<>();
        expectedActivities.add(ActivityTestUtils.createNormalActivity());
        List<SimplifiedActivity> activitySummaries = new ArrayList<>();
        for (Activity activity: expectedActivities) {
            activitySummaries.add(new SimplifiedActivity(activity));
        }
        SimplifiedActivitiesResponse responseBody = new SimplifiedActivitiesResponse(activitySummaries);
        ResponseEntity<SimplifiedActivitiesResponse> expectedResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
        Page<Activity> mockPage = new PageImpl<>(expectedActivities);
        Pageable mockRequest = PageRequest.of(startIndex / count, count);
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        Mockito.when(mockJwt.extractPermission(mockToken)).thenReturn(mockPermission);
        Mockito.when(mockJwt.extractId(mockToken)).thenReturn(mockProfileId);
        Mockito.when(mockActivitySearchService.getActivitiesByName(mockActivityName, mockProfileId, false,
                searchMethod, mockRequest)).thenReturn(mockPage);
        Mockito.when(mockService.createSimplifiedActivities(mockPage.getContent())).thenReturn(activitySummaries);
        ResponseEntity<SimplifiedActivitiesResponse> actualResponse =
                activitySearchController.searchActivitiesByName(mockActivityName, searchMethod, count, startIndex, mockToken);
        assertEquals(expectedResponse, actualResponse);
    }


    /**
     * Tests the getActivitiesByName method returns an unauthorised response when no token is given.
     */
    @Test
    void getActivitiesByNameWithNoTokenReturnsUnauthorizedResponseTest(){
        ResponseEntity<SimplifiedActivitiesResponse> response = activitySearchController.searchActivitiesByName(null,
                null, 0, 0, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Tests the getActivitiesByName method returns a bad request response when an invalid count is given.
     */
    @Test
    void getActivitiesByNameWithInvalidCountParameterReturnsBadRequestResponseTest(){
        String mockToken = "mockToken";
        Mockito.when(mockJwt.validateToken(mockToken)).thenReturn(true);
        ResponseEntity<SimplifiedActivitiesResponse> response = activitySearchController.searchActivitiesByName(null,
                null, 0, 0, mockToken);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


}