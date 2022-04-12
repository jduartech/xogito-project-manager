package com.xogito.manager.model.dto;

import com.xogito.manager.model.User;
import lombok.Data;


@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;

    public static UserDto from(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }
}
