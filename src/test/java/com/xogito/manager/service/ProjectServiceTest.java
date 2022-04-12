package com.xogito.manager.service;

import com.xogito.manager.fixtures.ProjectFixture;
import com.xogito.manager.fixtures.UserFixture;
import com.xogito.manager.model.Project;
import com.xogito.manager.model.User;
import com.xogito.manager.model.dto.Paging;
import com.xogito.manager.model.dto.PlainProjectDto;
import com.xogito.manager.repository.ProjectRepository;
import com.xogito.manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private ProjectRepository projectRepository;
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
        projectService = new ProjectService(projectRepository, userService);
    }

    @Test
    void canAddProject() {
        Project project = ProjectFixture.getSingleProject();
        projectService.createProject(project);
        ArgumentCaptor<Project> projectArgumentCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectArgumentCaptor.capture());
        Project capturedProject = projectArgumentCaptor.getValue();
        assertThat(capturedProject).isEqualTo(project);
    }

    @Test
    void givenProjectIdExistsShouldReturnProject() {
        Project project = ProjectFixture.getSingleProject();
        given(projectRepository.findById(project.getId())).willReturn(Optional.of(project));
        projectService.getProject(project.getId());
        verify(projectRepository).findById(project.getId());
    }

    @Test
    void willThrowWhenProjectIdDoesNotExists() {
        Long id = 10L;
        given(projectRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.getProject(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("Project id={%d} was not found", id));
        verify(projectRepository).findById(id);
    }

    @Test
    void canDeleteProject() {
        Project project = ProjectFixture.getSingleProject();
        Long id = project.getId();
        given(projectRepository.findById(id)).willReturn(Optional.of(project));
        projectService.deleteProject(id);
        verify(projectRepository).delete(project);
    }

    @Test
    void willThrowWhenDeleteProjectNotFound() {
        Long id = 10L;
        given(projectRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.deleteProject(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("Project id={%d} was not found", id));
        verify(projectRepository, never()).delete(any());
    }

    @Test
    void canUpdateProject() {
        Project project = ProjectFixture.getSingleProject();
        Project newDataProject = new Project(null, "Updated Name", "Updated Description", null);
        Project expectedProject = new Project(1L, "Updated Name", "Updated Description", new ArrayList<>());
        given(projectRepository.findById(project.getId())).willReturn(Optional.of(project));
        Project projectUpdated = projectService.updateProject(project.getId(), newDataProject);
        assertThat(projectUpdated).isEqualTo(expectedProject);
    }

    @Test
    void willThrowWhenUpdateProjectNotFound() {
        Long id = 10L;
        given(projectRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.updateProject(id, any()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("Project id={%d} was not found", id));
    }

    @Test
    void canAddUserToProject() {
        Project project = ProjectFixture.getSingleProject();
        User user = UserFixture.getSingleUser();
        List<User> users = new ArrayList<>();
        users.add(user);
        Project expectedProject = ProjectFixture.getSingleProject();
        expectedProject.setUsers(users);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(projectRepository.findById(project.getId())).willReturn(Optional.of(project));
        Project projectUpdated = projectService.addUser(project.getId(), user.getId());
        assertThat(projectUpdated).isEqualTo(expectedProject);
    }

    @Test
    void willThrowWhenAddUserIsAlreadyAssignedToProject() {
        Project project = ProjectFixture.getSingleProject();
        User user = UserFixture.getSingleUser();
        List<User> users = new ArrayList<>();
        users.add(user);
        project.setUsers(users);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(projectRepository.findById(project.getId())).willReturn(Optional.of(project));
        assertThatThrownBy(() -> projectService.addUser(project.getId(), user.getId()))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(String.format(
                        "User id={%d} is already assigned to Project id={%d}",
                        user.getId(), project.getId()));
    }

    @Test
    void willThrowWhenAddUserButProjectNotFound() {
        Project project = ProjectFixture.getSingleProject();
        given(projectRepository.findById(project.getId())).willReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.addUser(project.getId(),anyLong()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("Project id={%d} was not found", project.getId()));
    }

    @Test
    void willThrowWhenAddUserButUserNotFound() {
        Project project = ProjectFixture.getSingleProject();
        User user = UserFixture.getSingleUser();
        given(projectRepository.findById(anyLong())).willReturn(Optional.of(project));
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.addUser(anyLong(),user.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("User id={%d} was not found", user.getId()));
    }

    @Test
    void canRemoveUserToProject() {
        Project project = ProjectFixture.getSingleProject();
        User user = UserFixture.getSingleUser();
        List<User> users = new ArrayList<>();
        users.add(user);
        project.setUsers(users);
        Project expectedProject = ProjectFixture.getSingleProject();
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(projectRepository.findById(project.getId())).willReturn(Optional.of(project));
        Project projectUpdated = projectService.removeUser(project.getId(), user.getId());
        assertThat(projectUpdated).isEqualTo(expectedProject);
    }

    @Test
    void willThrowWhenRemoveUserIsNotAssignedToProject() {
        Project project = ProjectFixture.getSingleProject();
        User user = UserFixture.getSingleUser();
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(projectRepository.findById(project.getId())).willReturn(Optional.of(project));
        assertThatThrownBy(() -> projectService.removeUser(project.getId(), user.getId()))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(String.format(
                        "User id={%d} is not assigned to Project id={%d}",
                        user.getId(), project.getId()));
    }

    @Test
    void willThrowWhenRemoveUserButProjectNotFound() {
        Project project = ProjectFixture.getSingleProject();
        given(projectRepository.findById(project.getId())).willReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.removeUser(project.getId(),anyLong()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("Project id={%d} was not found", project.getId()));
    }

    @Test
    void willThrowWhenRemoveUserButUserNotFound() {
        Project project = ProjectFixture.getSingleProject();
        User user = UserFixture.getSingleUser();
        given(projectRepository.findById(anyLong())).willReturn(Optional.of(project));
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.removeUser(anyLong(),user.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("User id={%d} was not found", user.getId()));
    }

    @Test
    void canGetProjectsBySearchPageableSingleSort() {
        List<Project> projects = ProjectFixture.generateProjects("Projects Test", 10);
        String search = "testing";
        int page = 1;
        int limit = 10;
        String[] sort = {"id","desc"};

        List<Sort.Order> orders = new ArrayList<>();
        Sort.Direction direction = sort[1].contains("desc")? Sort.Direction.DESC : Sort.Direction.ASC;
        orders.add(new Sort.Order(direction, sort[0]));
        Pageable pagingSort = PageRequest.of(page-1, limit, Sort.by(orders));

        Page<Project> pageProjects = new PageImpl<>(projects);

        Paging paging = new Paging(1, limit, 1);
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("data", projects.stream().map(PlainProjectDto::from).collect(Collectors.toList()));
        expectedResult.put("paging", paging);

        given(projectRepository.findAllBySearchPageable(search, pagingSort)).willReturn(pageProjects);
        Map<String, Object> results = projectService.getAllProjects(search, page, limit, sort);
        assertThat(results).isEqualTo(expectedResult);

    }

    @Test
    void canGetProjectsBySearchPageableMultipleSort() {
        List<Project> projects = ProjectFixture.generateProjects("Projects Test", 10);
        String search = "testing";
        int page = 1;
        int limit = 10;
        String[] sort = {"id,asc","name,desc"};

        List<Sort.Order> orders = new ArrayList<>();
        for (String sortOrder : sort) {
            String[] _sort = sortOrder.split(",");
            orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
        }
        Pageable pagingSort = PageRequest.of(page-1, limit, Sort.by(orders));

        Page<Project> pageProjects = new PageImpl<>(projects);

        Paging paging = new Paging(1, limit, 1);
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("data", projects.stream().map(PlainProjectDto::from).collect(Collectors.toList()));
        expectedResult.put("paging", paging);

        given(projectRepository.findAllBySearchPageable(search, pagingSort)).willReturn(pageProjects);
        Map<String, Object> results = projectService.getAllProjects(search, page, limit, sort);
        assertThat(results).isEqualTo(expectedResult);

    }

    private Sort.Direction getSortDirection(String direction) {
        return direction.contains("desc")? Sort.Direction.DESC : Sort.Direction.ASC;
    }
}
