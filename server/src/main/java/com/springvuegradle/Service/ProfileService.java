package com.springvuegradle.Service;

import com.springvuegradle.Model.Profile;
import com.springvuegradle.PassportCountryRepository;
import com.springvuegradle.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class ProfileService {

    @Autowired
    private ProfileRepository repository;
    @Autowired
    private PassportCountryRepository pcRepository;

    public String saveProfile(Profile profile) {
        return "";
    }

}