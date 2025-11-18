# Projects — devconnect-backend

This file consolidates everything in the repository that relates to the "projects" feature (models, DTOs, controller, service, repository, mapper). Use this as a reference for where fetching, creating, updating, and querying projects is implemented.

## Files related to Projects

- `src/main/java/org/devconnect/devconnectbackend/model/Project.java` — JPA entity for projects, fields, statuses, timestamps
- `src/main/java/org/devconnect/devconnectbackend/dto/ProjectRequestDTO.java` — DTO used for incoming create/update requests
- `src/main/java/org/devconnect/devconnectbackend/dto/ProjectResponseDTO.java` — DTO returned to clients
- `src/main/java/org/devconnect/devconnectbackend/repository/ProjectRepository.java` — Spring Data JPA repository with convenient query methods
- `src/main/java/org/devconnect/devconnectbackend/service/ProjectService.java` — Business logic for creating, updating, deleting, and querying projects
- `src/main/java/org/devconnect/devconnectbackend/controller/ProjectController.java` — REST endpoints for projects (/api/projects)
- `src/main/java/org/devconnect/devconnectbackend/utills/ProjectMapper.java` — Mapper between DTOs and entity

## Summary / Contract

- Inputs: `ProjectRequestDTO` for create/update operations
- Outputs: `ProjectResponseDTO` for read operations
- Success criteria: controller endpoints return appropriate DTOs and HTTP status codes; service throws runtime exceptions for not-found cases (can be improved to custom exceptions)
- Error modes: currently uses `RuntimeException` for missing entities (consider `EntityNotFoundException` / custom `NotFoundException` and controller advice)

## Quick usage

API endpoints exposed in `ProjectController` (base path `/api/projects`):

- POST `/create` — create a project
- PUT `/update/{projectId}` — update
- DELETE `/delete/{projectId}` — delete
- PATCH `/{projectId}/complete` — mark completed
- PATCH `/{projectId}/status?status=...` — update status
- GET `/{projectId}` — get by id
- GET `/` — get all projects
- GET `/developer/{devId}` — get by developer id
- GET `/client/{clientId}` — get by client id
- GET `/status/{status}` — get by status

## Source references

Below are the source files for quick reference.

### `model/Project.java`

```java
package org.devconnect.devconnectbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_seq_gen")
    @SequenceGenerator(name = "project_seq_gen", sequenceName = "project_seq", allocationSize = 1)
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "dev_id", nullable = false)
    private Long devId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.PENDING;

    @Column(name = "project_budget", precision = 10, scale = 2)
    private BigDecimal projectBudget;

    @Column(name = "timeline")
    private LocalDateTime timeline;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ProjectStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
```

### `dto/ProjectRequestDTO.java`

```java
package org.devconnect.devconnectbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRequestDTO {
    private String projectName;
    private Long devId;
    private Long clientId;
    private String description;
    private BigDecimal projectBudget;
    private LocalDateTime timeline;
}
```

### `dto/ProjectResponseDTO.java`

```java
package org.devconnect.devconnectbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.devconnect.devconnectbackend.model.Project;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponseDTO {
    private Long projectId;
    private String projectName;
    private Long devId;
    private Long clientId;
    private String description;
    private Project.ProjectStatus status;
    private BigDecimal projectBudget;
    private LocalDateTime timeline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### `repository/ProjectRepository.java`

```java
package org.devconnect.devconnectbackend.repository;

import org.devconnect.devconnectbackend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByDevId(Long devId);
    List<Project> findByClientId(Long clientId);
    List<Project> findByStatus(Project.ProjectStatus status);
    List<Project> findByDevIdAndStatus(Long devId, Project.ProjectStatus status);
    List<Project> findByClientIdAndStatus(Long clientId, Project.ProjectStatus status);
}
```

### `service/ProjectService.java`

```java
package org.devconnect.devconnectbackend.service;

import lombok.RequiredArgsConstructor;
import org.devconnect.devconnectbackend.dto.ProjectRequestDTO;
import org.devconnect.devconnectbackend.dto.ProjectResponseDTO;
import org.devconnect.devconnectbackend.model.Project;
import org.devconnect.devconnectbackend.repository.ProjectRepository;
import org.devconnect.devconnectbackend.utills.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    // Add a new project
    @Transactional
    public ProjectResponseDTO addProject(ProjectRequestDTO requestDTO) {
        Project project = projectMapper.toEntity(requestDTO);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponseDTO(savedProject);
    }

    // Update an existing project
    @Transactional
    public ProjectResponseDTO updateProject(Long projectId, ProjectRequestDTO requestDTO) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        projectMapper.updateEntityFromDTO(requestDTO, project);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponseDTO(updatedProject);
    }

    // Delete a project
    @Transactional
    public void deleteProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found with id: " + projectId);
        }
        projectRepository.deleteById(projectId);
    }

    // Mark project as completed
    @Transactional
    public ProjectResponseDTO markProjectAsCompleted(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        project.setStatus(Project.ProjectStatus.COMPLETED);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponseDTO(updatedProject);
    }

    // Update project status
    @Transactional
    public ProjectResponseDTO updateProjectStatus(Long projectId, Project.ProjectStatus status) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        project.setStatus(status);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponseDTO(updatedProject);
    }

    // Get project by ID
    public ProjectResponseDTO getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        return projectMapper.toResponseDTO(project);
    }

    // Get all projects
    public List<ProjectResponseDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(projectMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get projects by developer ID
    public List<ProjectResponseDTO> getProjectsByDevId(Long devId) {
        return projectRepository.findByDevId(devId).stream()
                .map(projectMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get projects by developer ID
    public List<ProjectResponseDTO> getProjectsByClientId(Long clientId) {
        return projectRepository.findByClientId(clientId).stream()
                .map(projectMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get projects by status
    public List<ProjectResponseDTO> getProjectsByStatus(Project.ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(projectMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
```

### `utills/ProjectMapper.java`

```java
package org.devconnect.devconnectbackend.utills;

import org.devconnect.devconnectbackend.dto.ProjectRequestDTO;
import org.devconnect.devconnectbackend.dto.ProjectResponseDTO;
import org.devconnect.devconnectbackend.model.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    /**
     * Map ProjectRequestDTO to Project entity
     */
    public Project toEntity(ProjectRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Project project = new Project();
        project.setProjectName(dto.getProjectName());
        project.setDevId(dto.getDevId());
        project.setClientId(dto.getClientId());
        project.setDescription(dto.getDescription());
        project.setProjectBudget(dto.getProjectBudget());
        project.setTimeline(dto.getTimeline());
        project.setStatus(Project.ProjectStatus.PENDING);

        return project;
    }

    /**
     * Map Project entity to ProjectResponseDTO
     */
    public ProjectResponseDTO toResponseDTO(Project project) {
        if (project == null) {
            return null;
        }

        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setProjectId(project.getProjectId());
        dto.setProjectName(project.getProjectName());
        dto.setDevId(project.getDevId());
        dto.setClientId(project.getClientId());
        dto.setDescription(project.getDescription());
        dto.setStatus(project.getStatus());
        dto.setProjectBudget(project.getProjectBudget());
        dto.setTimeline(project.getTimeline());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        return dto;
    }

    /**
     * Update existing Project entity from ProjectRequestDTO
     */
    public void updateEntityFromDTO(ProjectRequestDTO dto, Project project) {
        if (dto == null || project == null) {
            return;
        }

        if (dto.getProjectName() != null) {
            project.setProjectName(dto.getProjectName());
        }
        if (dto.getDescription() != null) {
            project.setDescription(dto.getDescription());
        }
        if (dto.getProjectBudget() != null) {
            project.setProjectBudget(dto.getProjectBudget());
        }
        if (dto.getTimeline() != null) {
            project.setTimeline(dto.getTimeline());
        }
    }
}
```

## Notes & next steps

- Fetching projects is already implemented in `ProjectService` and exposed via `ProjectController` (see endpoints above).
- Improvements to consider:
  - Replace `RuntimeException` with a custom `NotFoundException` and global `@ControllerAdvice` to return proper 404 responses.
  - Add pagination and filtering for `/api/projects` (e.g., pageable, search by name, budget range).
  - Add unit/integration tests for controller and service methods.

---

Generated on 2025-11-18.
