package nl.crashandlearn.rabo_bankaccount.controller;

import nl.crashandlearn.rabo_bankaccount.exception.GlobalExceptionHandler;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService service;

    @InjectMocks
    private UserController controller;

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void getCurrentUserData() throws Exception {
        User user = User.builder().id(1L).build();
        given(service.getCurrentUser()).willReturn(user);
                // when
        MockHttpServletResponse response = mvc.perform(
                        get("/user/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void getCurrentUserBalance() throws Exception {
        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/user/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void getUserById_existing() throws Exception {
        User user = User.builder().id(1L).build();
        given(service.findByIdWithAccounts(1L)).willReturn(Optional.of(user));
        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/user/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }
    @Test
    public void getUserById_missing() throws Exception {
        given(service.findByIdWithAccounts(1L)).willReturn(Optional.empty());
        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/user/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }
    @Test
    public void getAllUsers() throws Exception {
        User user = User.builder().id(1L).build();
        List<User> users = new ArrayList<>();
        users.add(user);
        given(service.getAllUsersWithAccounts()).willReturn(users);
        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/user/all")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }
}
