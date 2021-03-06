package com.springvuegradle.repositories;

import com.springvuegradle.model.Email;
import com.springvuegradle.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface EmailRepository extends JpaRepository<Email, Long> {

    List<Email> findAllByAddress(String address);

    Optional<Email> findByAddress(String address);

    Boolean existsByAddress(String address);

    @Query("SELECT e.profile FROM Email e WHERE e.address = :email AND e.isPrimary = true")
    List<Profile> findByPrimaryEmail(@Param("email") String email);

    @Query("SELECT e.profile FROM Email e WHERE e.address = :email")
    Profile findProfileByEmail(@Param("email") String email);

    @Query("SELECT e.address FROM Email e WHERE e.profile = :profile AND e.isPrimary = true")
    String findPrimaryByProfile(@Param("profile") Profile profile);
}
