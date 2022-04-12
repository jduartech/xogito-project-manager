package com.xogito.manager.controller;

import com.xogito.manager.model.Project;
import com.xogito.manager.model.User;
import com.xogito.manager.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class ProjectControllerTest {

    @Mock
    private ProjectService projectService;
    private Project project;
    private Project projectUpdated;
    private Project projectWithUsers;
    private Map<String, Object> projectMap;

    @InjectMocks
    private ProjectController projectController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        project = new Project();
        project.setId(1L);
        project.setName("Test Project 1");
        project.setDescription("Description 1");

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("user@test.com");

    }
}
