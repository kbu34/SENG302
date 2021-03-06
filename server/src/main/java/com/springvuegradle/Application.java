package com.springvuegradle;

import com.springvuegradle.repositories.*;
import com.springvuegradle.utilities.InitialDataHelper;
import com.springvuegradle.utilities.ValidationHelper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@SpringBootApplication
@ComponentScan({"com.springvuegradle.controller", "com.springvuegradle.utilities", "com.springvuegradle.service"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner init(EmailRepository eRepo, ProfileRepository pRepo,
                           PassportCountryRepository pcRepo, ActivityTypeRepository atRepo,
                           ActivityRepository aRepo, ActivityMembershipRepository amRepo) {
        return args -> {
            System.out.println("-----Updating Activity Type and Profile Repositories-----");
            String password = InitialDataHelper.init(atRepo, pRepo, eRepo);
            if (password != null) {
                System.out.println("Default admin created with password: " + password);
            }
            System.out.println("-----Updating Passport Country Repository-----");
            ValidationHelper.updatePassportCountryRepository(pcRepo, pRepo);
            System.out.println("-----Program should be running now-----");
        };
    }

    // Fix the CORS errors
    @Bean
    public FilterRegistrationBean simpleCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // *** URL below needs to match the Vue client URL and port ***
        config.setAllowedOrigins(new ArrayList(Arrays.asList("http://localhost:9000", "http://localhost:9499", "http://localhost:9500", "https://csse-s302g1.canterbury.ac.nz/test", "https://csse-s302g1.canterbury.ac.nz/prod", "https://csse-s302g1.canterbury.ac.nz", "https://csse-s302g1.canterbury.ac.nz/test/api", "https://csse-s302g1.canterbury.ac.nz/prod/api")));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

}