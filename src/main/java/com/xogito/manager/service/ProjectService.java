package com.xogito.manager.service;

import com.xogito.manager.model.Project;
import com.xogito.manager.model.User;
import com.xogito.manager.model.dto.Paging;
import com.xogito.manager.model.dto.PlainProjectDto;
import com.xogito.manager.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public Project getProject(Long id) {
        return projectRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Project id={%d} was not found", id)));
    }

    public Map<String, Object> getAllProjects(String search, int page, int limit, String[] sort) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sort[0].contains(",")) {
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
            }
        } else {
            orders.add(new Sort.Order(getSortDirection(sort[1]), sort[0]));
        }
        Pageable pagingSort = PageRequest.of(page-1, limit, Sort.by(orders));
        Page<Project> pageProjects = projectRepository.findAll(search, pagingSort);
        Paging paging = new Paging(pageProjects.getNumber()+1, pageProjects.getNumberOfElements(), pageProjects.getTotalPages());
        List<PlainProjectDto> projects = pageProjects.getContent().stream().map(PlainProjectDto::from).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("data", projects);
        response.put("paging", paging);
        return response;
    }

    private Sort.Direction getSortDirection(String direction) {
        return direction.contains("desc")? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    public Project deleteProject(Long id) {
        Project project = getProject(id);
        projectRepository.delete(project);
        return project;
    }

    @Transactional
    public Project updateProject(Long id, Project project) {
        Project projectToEdit = getProject(id);
        if (project.getName() != null && !project.getName().isBlank()) {
            projectToEdit.setName(project.getName());
        }
        if (project.getDescription() != null && !project.getName().isBlank()) {
            projectToEdit.setDescription(project.getDescription());
        }
        return projectToEdit;
    }

    @Transactional
    public Project addUser(Long projectId, Long userId) {
        Project project = getProject(projectId);
        User user = userService.getUser(userId);
        if (project.getUsers().contains(user)) {
            throw new DataIntegrityViolationException(String.format("User id={%d} is already assigned to Project id={%d}", userId, projectId));
        }
        project.addUser(user);
        return project;
    }

    @Transactional
    public Project removeUser(Long projectId, Long userId) {
        Project project = getProject(projectId);
        User user = userService.getUser(userId);
        if (!project.getUsers().contains(user)) {
            throw new DataIntegrityViolationException(String.format("User id={%d} is not assigned to Project id={%d}", userId, projectId));
        }
        project.removeUser(user);
        return project;
    }

}
