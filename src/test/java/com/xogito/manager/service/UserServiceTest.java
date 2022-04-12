package com.xogito.manager.service;

import com.xogito.manager.fixtures.UserFixture;
import com.xogito.manager.model.User;
import com.xogito.manager.model.dto.Paging;
import com.xogito.manager.model.dto.UserDto;
import com.xogito.manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Order;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void canAddUser() {
        User user = UserFixture.getSingleUser();
        userService.createUser(user);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser).isEqualTo(user);
    }

    @Test
    void willThrowWhenEmailExists() {
        User user = UserFixture.getSingleUser();
        given(userRepository.existsByEmail(anyString())).willReturn(true);
        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(String.format("The email %s is already registered", user.getEmail()));
        verify(userRepository, never()).save(user);

    }

    @Test
    void givenUserIdExistsShouldReturnUser() {
        User user = UserFixture.getSingleUser();
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        userService.getUser(user.getId());
        verify(userRepository).findById(user.getId());
    }

    @Test
    void willThrowWhenUserIdDoesNotExists() {
        Long id = 10L;
        given(userRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUser(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("User id={%d} was not found", id));
        verify(userRepository).findById(id);
    }

    @Test
    void canDeleteUser() {
        User user = UserFixture.getSingleUser();
        Long id = user.getId();
        given(userRepository.findById(id)).willReturn(Optional.of(user));
        userService.deleteUser(id);
        verify(userRepository).delete(user);
    }

    @Test
    void willThrowWhenDeleteUserNotFound() {
        Long id = 10L;
        given(userRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> userService.deleteUser(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("User id={%d} was not found", id));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void canUpdateUser() {
        User user = UserFixture.getSingleUser();
        User newDataUser = new User(null, "Updated Name", "updated@gmail.com");
        User expectedUser = new User(1L, "Updated Name", "updated@gmail.com");
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        User userUpdated = userService.updateUser(user.getId(), newDataUser);
        assertThat(userUpdated).isEqualTo(expectedUser);
    }

    @Test
    void willThrowWhenUpdateUserNotFound() {
        Long id = 10L;
        given(userRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser(id, any()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("User id={%d} was not found", id));
    }

    @Test
    void canGetUsersBySearchPageableSingleSort() {
        List<User> users = UserFixture.generateUsers("Testing", 10);
        String search = "testing";
        int page = 1;
        int limit = 10;
        String[] sort = {"id","asc"};

        List<Sort.Order> orders = new ArrayList<>();
        Sort.Direction direction = sort[1].contains("desc")? Sort.Direction.DESC : Sort.Direction.ASC;
        orders.add(new Sort.Order(direction, sort[0]));
        Pageable pagingSort = PageRequest.of(page-1, limit, Sort.by(orders));

        Page<User> pageUsers = new PageImpl<>(users);

        Paging paging = new Paging(1, limit, 1);
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("data", users.stream().map(UserDto::from).collect(Collectors.toList()));
        expectedResult.put("paging", paging);

        given(userRepository.findAllBySearchPageable(search, pagingSort)).willReturn(pageUsers);
        Map<String, Object> results = userService.getAllUsers(search, page, limit, sort);
        assertThat(results).isEqualTo(expectedResult);

    }

    @Test
    void canGetUsersBySearchPageableMultipleSort() {
        List<User> users = UserFixture.generateUsers("Testing", 10);
        String search = "testing";
        int page = 1;
        int limit = 10;
        String[] sort = {"id,asc", "name,desc"};

        List<Order> orders = new ArrayList<>();
        for (String sortOrder : sort) {
            String[] _sort = sortOrder.split(",");
            orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
        }
        Pageable pagingSort = PageRequest.of(page-1, limit, Sort.by(orders));

        Page<User> pageUsers = new PageImpl<>(users);

        Paging paging = new Paging(1, limit, 1);
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("data", users.stream().map(UserDto::from).collect(Collectors.toList()));
        expectedResult.put("paging", paging);

        given(userRepository.findAllBySearchPageable(search, pagingSort)).willReturn(pageUsers);
        Map<String, Object> results = userService.getAllUsers(search, page, limit, sort);
        assertThat(results).isEqualTo(expectedResult);

    }

    private Sort.Direction getSortDirection(String direction) {
        return direction.contains("desc")? Sort.Direction.DESC : Sort.Direction.ASC;
    }

}
