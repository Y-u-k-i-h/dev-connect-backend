package org.devconnect.devconnectbackend.controller;

import lombok.RequiredArgsConstructor;
import org.devconnect.devconnectbackend.dto.DeveloperResponseDTO;
import org.devconnect.devconnectbackend.service.DeveloperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/developers")
@RequiredArgsConstructor
public class DeveloperController {

    private final DeveloperService developerService;

    /**
     * Get all developers
     * GET /api/developers
     */
    @GetMapping
    public ResponseEntity<List<DeveloperResponseDTO>> getAllDevelopers() {
        List<DeveloperResponseDTO> developers = developerService.getAllDevelopers();
        return ResponseEntity.ok(developers);
    }

    /**
     * Search developers by skills
     * GET /api/developers/search?skills={skillQuery}
     */
    @GetMapping("/search")
    public ResponseEntity<List<DeveloperResponseDTO>> searchDevelopers(
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal minRate,
            @RequestParam(required = false) BigDecimal maxRate) {
        
        List<DeveloperResponseDTO> developers;
        
        // Search by skills if provided
        if (skills != null && !skills.isEmpty()) {
            developers = developerService.searchDevelopersBySkills(skills);
        }
        // Filter by minimum rating
        else if (minRating != null) {
            developers = developerService.getDevelopersByMinRating(minRating);
        }
        // Filter by hourly rate range
        else if (minRate != null && maxRate != null) {
            developers = developerService.getDevelopersByHourlyRateRange(minRate, maxRate);
        }
        // Get all developers
        else {
            developers = developerService.getAllDevelopers();
        }
        
        return ResponseEntity.ok(developers);
    }

    /**
     * Get developer by ID
     * GET /api/developers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeveloperResponseDTO> getDeveloperById(@PathVariable Integer id) {
        DeveloperResponseDTO developer = developerService.getDeveloperById(id);
        return ResponseEntity.ok(developer);
    }

    /**
     * Get developer by user ID
     * GET /api/developers/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<DeveloperResponseDTO> getDeveloperByUserId(@PathVariable Integer userId) {
        DeveloperResponseDTO developer = developerService.getDeveloperByUserId(userId);
        return ResponseEntity.ok(developer);
    }

    /**
     * Get developers by minimum rating
     * GET /api/developers/rating/{minRating}
     */
    @GetMapping("/rating/{minRating}")
    public ResponseEntity<List<DeveloperResponseDTO>> getDevelopersByMinRating(@PathVariable BigDecimal minRating) {
        List<DeveloperResponseDTO> developers = developerService.getDevelopersByMinRating(minRating);
        return ResponseEntity.ok(developers);
    }
}
