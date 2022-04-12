package com.xogito.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xogito.manager.fixtures.ProjectFixture;
import com.xogito.manager.fixtures.UserFixture;
import com.xogito.manager.model.Project;
import com.xogito.manager.model.User;
import com.xogito.manager.model.dto.Paging;
import com.xogito.manager.model.dto.PlainProjectDto;
import com.xogito.manager.repository.ProjectRepository;
import com.xogito.manager.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception{
        projectRepository.deleteAll();
        userRepository.deleteAll();
        autoCloseable.close();
    }

    @Test
    public void shouldCreateAProject() throws Exception {
        Project project = ProjectFixture.getSingleProject();

        ResultActions resultActions = mockMvc.perform(post("/api/v1/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(PlainProjectDto.from(project))));
        resultActions.andExpect(status().isCreated());
        List<Project> projects = projectRepository.findAll();
        assertThat(projects.size()).isEqualTo(1);
    }

    @Test
    public void shouldGetAllProjectsWithPaging() throws Exception {
        List<Project> someProjects = ProjectFixture.generateProjects("Testing Project", 10);
        for (Project project : someProjects) {
            projectRepository.save(project);
        }
        List<PlainProjectDto> expectedProjects = someProjects.stream().map(PlainProjectDto::from).collect(Collectors.toList());
        String search = "test";
        int page = 1;
        int totalElements = 10;
        int totalPages = 1;
        Paging paging = new Paging(page, totalElements, totalPages);
        Map<String, Object> result = new HashMap<>();
        result.put("data", expectedProjects);
        result.put("paging", paging);
        ResultActions resultActions = mockMvc.perform(get(String.format("/api/v1/projects?search=%s&sort=id,asc&limit=10",search))
                            .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().string(objectMapper.writeValueAsString(result)));
    }

    @Test
    public void havingProjectsShouldGetEmptyResultWithPaging() throws Exception {
        List<Project> someProjects = ProjectFixture.generateProjects("Testing Project", 10);
        for (Project project : someProjects) {
            projectRepository.save(project);
        }
        String search = "miss";
        int page = 1;
        int totalElements = 0;
        int totalPages = 0;
        Paging paging = new Paging(page, totalElements, totalPages);
        Map<String, Object> result = new HashMap<>();
        result.put("data", new ArrayList<>());
        result.put("paging", paging);
        ResultActions resultActions = mockMvc.perform(get(String.format("/api/v1/projects?search=%s&sort=id,asc&limit=10",search))
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().string(objectMapper.writeValueAsString(result)));
    }

    @Test
    public void shouldReturnRespectiveProjectById() throws Exception {
        Project project = ProjectFixture.getSingleProject();
        Project dbProject = projectRepository.save(project);
        Long id = dbProject.getId();
        ResultActions resultActions = mockMvc.perform(get(String.format("/api/v1/projects/%d", id))
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().string(objectMapper.writeValueAsString(dbProject)));
    }

    @Test
    public void shouldUpdateProjectNameAndDescription() throws Exception {
        Project project = ProjectFixture.getSingleProject();
        Project dbProject = projectRepository.save(project);
        Long id = dbProject.getId();

        String newName = "Project Updated";
        dbProject.setName(newName);

        PlainProjectDto data = new PlainProjectDto();
        data.setName(newName);

        ResultActions resultActions = mockMvc.perform(put(String.format("/api/v1/projects/%d", id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data)));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().string(objectMapper.writeValueAsString(dbProject)));
    }

    @Test
    public void shouldAddUserToProject() throws Exception {
        Project project = ProjectFixture.getSingleProject();
        Project dbProject = projectRepository.save(project);
        Long projectId = dbProject.getId();

        User user = UserFixture.getSingleUser();
        User dbUser = userRepository.save(user);
        Long userId = dbUser.getId();
        List<User> users = new ArrayList<>();
        users.add(dbUser);

        dbProject.setUsers(users);

        ResultActions resultActions = mockMvc.perform(post(String.format("/api/v1/projects/%d/users/%d/add", projectId, userId))
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().string(objectMapper.writeValueAsString(dbProject)));
    }

    @Test
    public void shouldRemoveUserFromProject() throws Exception {
        Project project = ProjectFixture.getSingleProject();
        Project dbProject = projectRepository.save(project);
        Long projectId = dbProject.getId();

        User user = UserFixture.getSingleUser();

        User dbUser = userRepository.save(user);
        Long userId = dbUser.getId();

        List<User> users = new ArrayList<>();
        users.add(dbUser);

        dbProject.setUsers(users);
        projectRepository.saveAndFlush(dbProject);

        Project projectExpected = new Project(dbProject.getId(), dbProject.getName(), dbProject.getDescription(), new ArrayList<>());

        ResultActions resultActions = mockMvc.perform(delete(String.format("/api/v1/projects/%d/users/%d/remove", projectId, userId))
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().string(objectMapper.writeValueAsString(projectExpected)));
    }


    @Test
    public void shouldDeleteProject() throws Exception {
        Project project = ProjectFixture.getSingleProject();
        Project dbProject = projectRepository.save(project);

        PlainProjectDto dataExpected = PlainProjectDto.from(dbProject);

        ResultActions resultActions = mockMvc.perform(delete(String.format("/api/v1/projects/%d", dbProject.getId()))
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().string(objectMapper.writeValueAsString(dataExpected)));
    }

}
