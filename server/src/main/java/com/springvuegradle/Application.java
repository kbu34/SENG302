package com.springvuegradle;

import com.springvuegradle.Model.PassportCountry;
import com.springvuegradle.Utiilities.ValidationHelper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

import java.util.*;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication @ComponentScan({"com.springvuegradle.Controller"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner init(ProfileRepository profileRepository, PassportCountryRepository passportCountryRepository) {
        return args -> {
            List<PassportCountry> countries = new ArrayList<PassportCountry>();
            for (String s : ValidationHelper.GetRESTCountries()) {
                countries.add(new PassportCountry(s));
            }
            if (passportCountryRepository.count() == 0) {
                passportCountryRepository.saveAll(countries);
            }
            profileRepository.findAll().forEach(System.out::println); // prints all the profile objects in the repository.
        };
    }

    // Fix the CORS errors
    @Bean
    public FilterRegistrationBean simpleCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // *** URL below needs to match the Vue client URL and port ***
        config.setAllowedOrigins(new ArrayList(Arrays.asList("http://localhost:9000", "http://localhost:9500", "https://csse-s302g0.canterbury.ac.nz/test", "https://csse-s302g0.canterbury.ac.nz/prod")));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

}