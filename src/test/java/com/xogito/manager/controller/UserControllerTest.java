package com.xogito.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xogito.manager.model.User;
import com.xogito.manager.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    private User user;
    private User userUpdated;
    private Map<String, Object> userMap;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("user@test.com");

        userMap = new HashMap<>();

        userUpdated = new User();
        userUpdated.setId(1L);
        userUpdated.setName("Jon Doe");
        userUpdated.setEmail("user@test.com");

        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @AfterEach
    void tearDown() {
        user = null;
        userUpdated = null;
        userMap = null;
    }

    @Test
    public void PostMappingOfUser() throws Exception {
        when(userService.createUser(any())).thenReturn(user);
        mockMvc.perform(post("/api/v1/users").
                contentType(MediaType.APPLICATION_JSON).
                content(asJsonString(user))).
                andExpect(status().isCreated());
        verify(userService, times(1)).createUser(any());
    }

    @Test
    public void GetMappingOfAllUsersWithSearchMatch() throws Exception {
        String search = "test";
        int page = 1;
        int limit = 10;
        String[] sort = {"id","desc"};
        when(userService.getAllUsers(search, page, limit, sort)).thenReturn(userMap);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users?search=test").
                contentType(MediaType.APPLICATION_JSON)).
                andDo(MockMvcResultHandlers.print());
        verify(userService).getAllUsers(search, page, limit, sort);
        verify(userService, times(1)).getAllUsers(search, page, limit, sort);

    }

    @Test
    public void GetMappingOfAllUsersEmptySearchMatch() throws Exception {
        String search = "Jon";
        int page = 1;
        int limit = 10;
        String[] sort = {"id","desc"};
        when(userService.getAllUsers(search, page, limit, sort)).thenReturn(userMap);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users?search=Jon").
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(MockMvcResultMatchers.status().isOk()).
                andDo(MockMvcResultHandlers.print());
        verify(userService).getAllUsers(search, page, limit, sort);
        verify(userService, times(1)).getAllUsers(search, page, limit, sort);

    }

    @Test
    public void GetMappingOfUserShouldReturnRespectiveUser() throws Exception {
        when(userService.getUser(user.getId())).thenReturn(user);
        mockMvc.perform(get("/api/v1/users/1").
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(MockMvcResultMatchers.status().isOk())
                        .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void PutMappingOfUser() throws Exception {
        User newUser = new User();
        newUser.setName("Jon Doe");
        when(userService.updateUser(user.getId(), newUser)).thenReturn(userUpdated);
        mockMvc.perform(put("/api/v1/users/1").
                        contentType(MediaType.APPLICATION_JSON).
                        content(asJsonString(newUser))).
                        andExpect(status().isOk());
        verify(userService, times(1)).updateUser(user.getId(), newUser);
    }

    @Test
    public void DeleteMappingOfUser() throws Exception {
        when(userService.deleteUser(user.getId())).thenReturn(userUpdated);
        mockMvc.perform(delete("/api/v1/users/1").
                        contentType(MediaType.APPLICATION_JSON)).
                        andExpect(status().isOk());
        verify(userService, times(1)).deleteUser(user.getId());
    }



    private static String asJsonString(final Object obj){
        try{
            return new ObjectMapper().writeValueAsString(obj);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}