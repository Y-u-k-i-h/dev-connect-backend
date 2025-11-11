package org.devconnect.devconnectbackend.repository;

import org.devconnect.devconnectbackend.model.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Integer> {

    Optional<Developer> findByUser_Id(Long id);

    Optional<Developer> findByUser_Email(String email);

    List<Developer> findByHourlyRateGreaterThanEqual(BigDecimal hourlyRate);

    List<Developer> findByHourlyRateLessThanEqual(BigDecimal hourlyRate);

    List<Developer> findByAverageRatingGreaterThanEqual(BigDecimal averageRating);

    List<Developer> findByAverageRatingLessThanEqual(BigDecimal averageRating);

    List<Developer> findByTotalProjectsCompletedGreaterThanEqual(Integer totalProjectsCompleted);

    List<Developer> findByTotalProjectsCompletedLessThanEqual(Integer totalProjectsCompleted);
}
