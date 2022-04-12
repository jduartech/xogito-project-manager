package com.xogito.manager.controller;

import com.xogito.manager.model.User;
import com.xogito.manager.model.dto.UserDto;
import com.xogito.manager.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody final UserDto userDto) {
        User user = userService.createUser(User.from(userDto));
        return new ResponseEntity<>(UserDto.from(user), HttpStatus.CREATED);
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable final Long id) {
        User user = userService.getUser(id);
        return new ResponseEntity<>(UserDto.from(user), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "id,desc") String[] sort) {
        Map<String, Object> response = userService.getAllUsers(search, page, limit, sort);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(value = "{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable final Long id,
            @RequestBody final UserDto userDto) throws EntityNotFoundException {
        User user = userService.updateUser(id, User.from(userDto));
        return new ResponseEntity<>(UserDto.from(user), HttpStatus.OK);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable final Long id) throws EntityNotFoundException {
        User user = userService.deleteUser(id);
        return new ResponseEntity<>(UserDto.from(user), HttpStatus.OK);
    }

}
