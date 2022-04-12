package com.xogito.manager.fixtures;

import com.xogito.manager.model.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectFixture {
    public static List<Project> generateProjects(String name, int times) {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            Project project = new Project();
            project.setName(String.format("%s of %d", name, i));
            projects.add(project);
        }
        return projects;
    }
    public static Project getSingleProject() {
        Project project = new Project(1L, "Testing Project", "", new ArrayList<>());
        return project;
    }
}
