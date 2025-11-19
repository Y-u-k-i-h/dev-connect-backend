package org.devconnect.devconnectbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devconnect.devconnectbackend.dto.ProjectRequestDTO;
import org.devconnect.devconnectbackend.dto.ProjectResponseDTO;
import org.devconnect.devconnectbackend.exception.ProjectAlreadyClaimedException;
import org.devconnect.devconnectbackend.exception.ProjectNotFoundException;
import org.devconnect.devconnectbackend.model.Project;
import org.devconnect.devconnectbackend.repository.ProjectRepository;
import org.devconnect.devconnectbackend.utills.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    /**
     * Get available projects (unclaimed projects with PENDING status)
     * These are projects that developers can claim
     */
    public List<ProjectResponseDTO> getAvailableProjects() {
        return projectRepository.findAll().stream()
                .filter(project -> project.getDevId() == null && 
                                   project.getStatus() == Project.ProjectStatus.PENDING)
                .map(projectMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Atomically claim a project for a developer.
     * This method ensures that only one developer can claim a project at a time.
     * 
     * @param projectId The ID of the project to claim
     * @param devId The ID of the developer claiming the project
     * @return The updated project
     * @throws ProjectNotFoundException if the project doesn't exist
     * @throws ProjectAlreadyClaimedException if the project is already claimed
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ProjectResponseDTO claimProject(Long projectId, Long devId) {
        log.info("Attempting to claim project {} for developer {}", projectId, devId);
        
        // Fetch the project with pessimistic write lock to prevent concurrent claims
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
        
        // Verify project is available for claiming
        if (project.getDevId() != null) {
            log.warn("Project {} already claimed by developer {}", projectId, project.getDevId());
            throw new ProjectAlreadyClaimedException(
                "Project is already claimed by developer ID: " + project.getDevId()
            );
        }
        
        if (project.getStatus() != Project.ProjectStatus.PENDING) {
            log.warn("Project {} is not available for claiming. Current status: {}", projectId, project.getStatus());
            throw new ProjectAlreadyClaimedException(
                "Project is not available for claiming. Current status: " + project.getStatus()
            );
        }
        
        // Atomically update both fields
        project.setDevId(devId);
        project.setStatus(Project.ProjectStatus.IN_PROGRESS);
        
        Project claimedProject = projectRepository.save(project);
        log.info("Successfully claimed project {} for developer {}", projectId, devId);
        
        return projectMapper.toResponseDTO(claimedProject);
    }
}
