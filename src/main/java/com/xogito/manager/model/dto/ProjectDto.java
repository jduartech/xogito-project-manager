package com.xogito.manager.model.dto;

import com.xogito.manager.model.Project;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Data
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private Collection<UserDto> users;

    public static ProjectDto from(Project project) {
        ProjectDto projectDto = new ProjectDto();
        projectDto.setId(project.getId());
        projectDto.setName(project.getName());
        projectDto.setDescription(project.getDescription());
        if (project.getUsers() != null) {
            projectDto.users = project.getUsers().stream().map(UserDto::from).collect(Collectors.toList());
        }
        return projectDto;
    }

}
