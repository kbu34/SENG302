package com.springvuegradle.Controller;


import com.springvuegradle.Utilities.JwtUtil;
import com.springvuegradle.dto.LoginRequest;
import com.springvuegradle.dto.LoginResponse;
import com.springvuegradle.dto.LogoutRequest;
import com.springvuegradle.Model.Profile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class LoginControllerTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LoginController loginController;

    @Autowired
    private Profile_Controller profileController;


    /**
     * This test ensures profiles login in properly with the correct credentials
     * using the loginUser method
     */
    @BeforeEach
    void beforeEach() {
        Profile maurice = createNormalProfileMaurice();
        profileController.createProfile(maurice);
    }

    //@Test
    void adminAccessTest() {

        Profile jimmy = ProfileControllerTest.createNormalProfileJimmy();
        LoginRequest jimmysRequest = new LoginRequest("jimmy@yahoo.com", "asdf");
        int expected_session_counter = 0;
        // Tests that an unregistered user cannot login
        ResponseEntity<LoginResponse> response_entity = loginController.loginUser(jimmysRequest);
        assertEquals(HttpStatus.UNAUTHORIZED, response_entity.getStatusCode());
        // Session counter should notincrement when a user logs in.
        assertEquals(expected_session_counter, loginController.getSessionCounter());

        // Test to check successfully created Users cannot log in with the correct email but incorrect password
        LoginRequest mauricesRequest1 = new LoginRequest("jacky@google.com", "1234");
        ResponseEntity<LoginResponse> response_entity_maurice1 = loginController.loginUser(mauricesRequest1);
        assertEquals(HttpStatus.UNAUTHORIZED, response_entity_maurice1.getStatusCode());

        // Test to check successfully created Users cannot log in with the incorrect email but correct password
        LoginRequest mauricesRequest2 = new LoginRequest("phillip@google.com", "jacky'sSecuredPwd");
        ResponseEntity<LoginResponse> response_entity_maurice2 = loginController.loginUser(mauricesRequest2);
        assertEquals(HttpStatus.UNAUTHORIZED, response_entity_maurice2.getStatusCode());

        // Test to check user can successfully login when providing the correct email and correct password.
        LoginRequest mauricesRequest3 = new LoginRequest("jacky@google.com", "jacky'sSecuredPwd");
        ResponseEntity<LoginResponse> response_entity_maurice3 = loginController.loginUser(mauricesRequest3);
        assertEquals(HttpStatus.OK, response_entity_maurice3.getStatusCode());
        expected_session_counter += 1;
        // Checks session_counter increments correctly upon a successful login
        assertEquals(expected_session_counter, loginController.getSessionCounter());
    }

    @Test
    void loginFailureTest() {
        Profile jimmy = ProfileControllerTest.createNormalProfileJimmy();
        LoginRequest request = new LoginRequest("jimmy@yahoo.com", "invalid-password");
        ResponseEntity<LoginResponse> response_entity = loginController.loginUser(request);
        assertEquals(HttpStatus.UNAUTHORIZED, response_entity.getStatusCode());
    }

    @Test
    void loginTest() {

        // Test to check user can successfully login when providing the correct email and correct password.
        LoginRequest jackyRequest = new LoginRequest("jacky@google.com", "jacky'sSecuredPwd");
        ResponseEntity<LoginResponse> response = loginController.loginUser(jackyRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(jwtUtil.validateToken(response.getBody().getToken(), response.getBody().getUserId()));
    }

    /**
     * @return a valid profile object.
     */
    static Profile createNormalProfileMaurice() {
        return new Profile(null, "Maurice", "Benson", "Jack", "Jacky", "jacky@google.com", new String[]{"additionaldoda@email.com"}, "jacky'sSecuredPwd",
                "Jacky loves to ride his bike on crazy mountains.", new GregorianCalendar(1985, Calendar.DECEMBER,
                20), "male", 1, new String[]{"New Zealand", "India"}, new String[]{}, 5);
    }


}
