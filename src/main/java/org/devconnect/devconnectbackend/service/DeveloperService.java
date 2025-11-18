package org.devconnect.devconnectbackend.service;

import lombok.RequiredArgsConstructor;
import org.devconnect.devconnectbackend.dto.DeveloperResponseDTO;
import org.devconnect.devconnectbackend.model.Developer;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.DeveloperRepository;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeveloperService {

    private final DeveloperRepository developerRepository;
    private final UserRepository userRepository;

    /**
     * Get all developers with their user information
     */
    public List<DeveloperResponseDTO> getAllDevelopers() {
        return developerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search developers by skills (case-insensitive)
     */
    public List<DeveloperResponseDTO> searchDevelopersBySkills(String skillQuery) {
        return developerRepository.findAll().stream()
                .filter(dev -> dev.getSkills() != null && 
                              dev.getSkills().toLowerCase().contains(skillQuery.toLowerCase()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get developers by minimum rating
     */
    public List<DeveloperResponseDTO> getDevelopersByMinRating(BigDecimal minRating) {
        return developerRepository.findByAverageRatingGreaterThanEqual(minRating).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get developers by hourly rate range
     */
    public List<DeveloperResponseDTO> getDevelopersByHourlyRateRange(BigDecimal minRate, BigDecimal maxRate) {
        return developerRepository.findAll().stream()
                .filter(dev -> {
                    BigDecimal rate = dev.getHourlyRate();
                    return rate != null && rate.compareTo(minRate) >= 0 && rate.compareTo(maxRate) <= 0;
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get developer by user ID
     */
    public DeveloperResponseDTO getDeveloperByUserId(Integer userId) {
        Developer developer = developerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Developer not found for user ID: " + userId));
        return convertToDTO(developer);
    }

    /**
     * Get developer by ID
     */
    public DeveloperResponseDTO getDeveloperById(Integer developerId) {
        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new RuntimeException("Developer not found with ID: " + developerId));
        return convertToDTO(developer);
    }

    /**
     * Convert Developer entity to DTO with user information
     */
    private DeveloperResponseDTO convertToDTO(Developer developer) {
        User user = userRepository.findById(developer.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found for developer"));

        return DeveloperResponseDTO.builder()
                .developerId(developer.getDeveloperId())
                .userId(developer.getUserId())
                .username(developer.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(developer.getBio())
                .skills(developer.getSkills())
                .hourlyRate(developer.getHourlyRate())
                .githubUrl(developer.getGithubUrl())
                .linkedinUrl(developer.getLinkedinUrl())
                .portfolioUrl(developer.getPortfolioUrl())
                .averageRating(developer.getAverageRating())
                .totalProjectsCompleted(developer.getTotalProjectsCompleted())
                .userStatus(user.getUserStatus())
                .build();
    }
}
