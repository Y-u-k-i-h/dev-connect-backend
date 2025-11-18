package org.devconnect.devconnectbackend.controller;

import lombok.RequiredArgsConstructor;
import org.devconnect.devconnectbackend.dto.ProjectRequestDTO;
import org.devconnect.devconnectbackend.dto.ProjectResponseDTO;
import org.devconnect.devconnectbackend.model.Project;
import org.devconnect.devconnectbackend.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // Create a new project
    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDTO> createProject(@RequestBody ProjectRequestDTO requestDTO) {
        try {
            System.out.println("Creating project with data: " + requestDTO);
            ProjectResponseDTO response = projectService.addProject(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.err.println("Error creating project: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Update an existing project
    @PutMapping("/update/{projectId}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable Long projectId,
            @RequestBody ProjectRequestDTO requestDTO) {
        ProjectResponseDTO response = projectService.updateProject(projectId, requestDTO);
        return ResponseEntity.ok(response);
    }

    // Delete a project
    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<Map<String, String>> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Project deleted successfully");
        return ResponseEntity.ok(response);
    }

    // Mark project as completed
    @PatchMapping("/{projectId}/complete")
    public ResponseEntity<ProjectResponseDTO> markProjectAsCompleted(@PathVariable Long projectId) {
        ProjectResponseDTO response = projectService.markProjectAsCompleted(projectId);
        return ResponseEntity.ok(response);
    }

    // Update project status
    @PatchMapping("/{projectId}/status")
    public ResponseEntity<ProjectResponseDTO> updateProjectStatus(
            @PathVariable Long projectId,
            @RequestParam Project.ProjectStatus status) {
        ProjectResponseDTO response = projectService.updateProjectStatus(projectId, status);
        return ResponseEntity.ok(response);
    }

    // Get project by ID
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long projectId) {
        ProjectResponseDTO response = projectService.getProjectById(projectId);
        return ResponseEntity.ok(response);
    }

    // Get all projects
    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
        List<ProjectResponseDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    // Get projects by developer ID
    @GetMapping("/developer/{devId}")
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsByDevId(@PathVariable Long devId) {
        List<ProjectResponseDTO> projects = projectService.getProjectsByDevId(devId);
        return ResponseEntity.ok(projects);
    }

    // Get projects by client ID
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsByClientId(@PathVariable Long clientId) {
        List<ProjectResponseDTO> projects = projectService.getProjectsByClientId(clientId);
        return ResponseEntity.ok(projects);
    }

    // Get projects by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsByStatus(@PathVariable Project.ProjectStatus status) {
        List<ProjectResponseDTO> projects = projectService.getProjectsByStatus(status);
        return ResponseEntity.ok(projects);
    }
}
