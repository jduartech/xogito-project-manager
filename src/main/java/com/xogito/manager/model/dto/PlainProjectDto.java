package com.xogito.manager.model.dto;

import com.xogito.manager.model.Project;
import lombok.Data;

@Data
public class PlainProjectDto {
    private Long id;
    private String name;
    private String description;

    public static PlainProjectDto from(Project project) {
        PlainProjectDto plainProjectDto = new PlainProjectDto();
        plainProjectDto.setId(project.getId());
        plainProjectDto.setName(project.getName());
        plainProjectDto.setDescription(project.getDescription());
        return plainProjectDto;
    }
}
