package com.xogito.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xogito.manager.fixtures.UserFixture;
import com.xogito.manager.model.User;
import com.xogito.manager.model.dto.Paging;
import com.xogito.manager.model.dto.UserDto;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception{
        userRepository.deleteAll();
        userRepository.deleteAll();
        autoCloseable.close();
    }

    @Test
    public void shouldCreateAnUser() throws Exception {
        User user = UserFixture.getSingleUser();
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UserDto.from(user))));
        resultActions.andExpect(status().isCreated());
        List<User> users = userRepository.findAll();
        assertThat(users.size()).isEqualTo(1);
    }

    @Test
    public void shouldGetAllUsersWithPaging() throws Exception {
        List<User> someUsers = UserFixture.generateUsers("karl", 10);
        for (User user : someUsers) {
            userRepository.save(user);
        }
        String search = "karl";
        int page = 1;
        int totalElements = 10;
        int totalPages = 1;
        Paging paging = new Paging(page, totalElements, totalPages);
        Map<String, Object> result = new HashMap<>();
        result.put("data", someUsers);
        result.put("paging", paging);
        ResultActions resultActions = mockMvc.perform(get(String.format("/api/v1/users?search=%s&sort=id,asc&limit=10",search))
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().string(objectMapper.writeValueAsString(result)));
    }

    @Test
    public void havingUsersShouldGetEmptyResultWithPaging() throws Exception {
        List<User> someUsers = UserFixture.generateUsers("karl", 10);
        for (User user : someUsers) {
            userRepository.save(user);
        }
        String search = "max";
        int page = 1;
        int totalElements = 0;
        int totalPages = 0;
        Paging paging = new Paging(page, totalElements, totalPages);
        Map<String, Object> result = new HashMap<>();
        result.put("data", new ArrayList<>());
        result.put("paging", paging);
        ResultActions resultActions = mockMvc.perform(get(String.format("/api/v1/users?search=%s&sort=id,asc&limit=10",search))
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().string(objectMapper.writeValueAsString(result)));

    }

    
    @Test
    public void shouldReturnRespectiveUser() throws Exception {
        User user = UserFixture.getSingleUser();
        User dbUser = userRepository.save(user);
        Long id = dbUser.getId();
        ResultActions resultActions = mockMvc.perform(get(String.format("/api/v1/users/%d", id))
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().string(objectMapper.writeValueAsString(dbUser)));
    }

    @Test
    public void shouldUpdateUserName() throws Exception {
        User user = UserFixture.getSingleUser();
        User dbUser = userRepository.save(user);
        Long id = dbUser.getId();

        String newName = "User Updated";
        dbUser.setName(newName);

        UserDto data = new UserDto();
        data.setName(newName);

        ResultActions resultActions = mockMvc.perform(put(String.format("/api/v1/users/%d", id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(data)));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().string(objectMapper.writeValueAsString(dbUser)));
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        User user = UserFixture.getSingleUser();
        User dbUser = userRepository.save(user);

        UserDto dataExpected = UserDto.from(dbUser);

        ResultActions resultActions = mockMvc.perform(delete(String.format("/api/v1/users/%d", dbUser.getId()))
                .contentType(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().string(objectMapper.writeValueAsString(dataExpected)));
    }

}