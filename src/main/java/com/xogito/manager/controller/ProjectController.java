package com.xogito.manager.controller;

import com.xogito.manager.model.Project;
import com.xogito.manager.model.dto.PlainProjectDto;
import com.xogito.manager.model.dto.ProjectDto;
import com.xogito.manager.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<PlainProjectDto> createProject(@Valid @RequestBody final ProjectDto projectDto) {
        Project project = projectService.createProject(Project.from(projectDto));
        return new ResponseEntity<>(PlainProjectDto.from(project), HttpStatus.CREATED);
    }

    @PostMapping(value = "{projectId}/users/{userId}/add")
    public ResponseEntity<ProjectDto> addUserToProject(@PathVariable final Long projectId, @PathVariable final Long userId) {
        Project project = projectService.addUser(projectId, userId);
        return new ResponseEntity<>(ProjectDto.from(project), HttpStatus.OK);
    }

    @DeleteMapping(value = "{projectId}/users/{userId}/remove")
    public ResponseEntity<ProjectDto> removeUserFromProject(@PathVariable final Long projectId, @PathVariable final Long userId) {
        Project project = projectService.removeUser(projectId, userId);
        return new ResponseEntity<>(ProjectDto.from(project), HttpStatus.OK);
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<ProjectDto> getProject(@PathVariable final Long id) {
        Project project = projectService.getProject(id);
        return new ResponseEntity<>(ProjectDto.from(project), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProjects(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "id,desc") String[] sort) {
        Map<String, Object> response = projectService.getAllProjects(search, page, limit, sort);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(value = "{id}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable final Long id,
                                              @RequestBody final ProjectDto projectDto) throws EntityNotFoundException {
        Project project = projectService.updateProject(id, Project.from(projectDto));
        return new ResponseEntity<>(ProjectDto.from(project), HttpStatus.OK);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<PlainProjectDto> deleteProject(@PathVariable final Long id) throws EntityNotFoundException {
        Project project = projectService.deleteProject(id);
        return new ResponseEntity<>(PlainProjectDto.from(project), HttpStatus.OK);
    }
}
