package org.devconnect.devconnectbackend.repository;

import org.devconnect.devconnectbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // List<User> findAll() already provided by JpaRepository

    // List<User> findById() already provided by JpaRepository

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByUserType(User.UserType userType);

    List<User> findByIsActive(Boolean isActive);

    List<User> findByIsVerified(Boolean isVerified);

}
