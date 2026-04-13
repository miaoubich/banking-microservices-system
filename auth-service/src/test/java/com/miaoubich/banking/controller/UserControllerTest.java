package com.miaoubich.banking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoubich.banking.domain.User;
import com.miaoubich.banking.domain.UserRole;
import com.miaoubich.banking.dto.UpdatePasswordRequest;
import com.miaoubich.banking.exception.UserNotFoundException;
import com.miaoubich.banking.service.UserService;

/*
 * Integration test for UserController using MockMvc to simulate HTTP requests and verify responses.
 */
@WebMvcTest(UserController.class)
@EnableMethodSecurity(prePostEnabled = true)
@Import(com.miaoubich.banking.exception.GlobalExceptionHandler.class)
class UserControllerTest {

    private final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Ali", "Bouzar", "miaoubich", "miaoubich@example.com", "0123456789", "kc-uuid-123", UserRole.client_user);
    }

    // --- Print all users -->  GET /api/users ---

    @Test
    void getAllUsers_returnsOk_whenClientSuper() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_super"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Ali"))
                .andExpect(jsonPath("$[0].lastName").value("Bouzar"))
                .andExpect(jsonPath("$[0].username").value("miaoubich"))
                .andExpect(jsonPath("$[0].email").value("miaoubich@example.com"))
                .andExpect(jsonPath("$[0].role").value("client_user"));
    }

    @Test
    void getAllUsers_returnsForbidden_whenClientAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_admin"))))
                .andExpect(status().isForbidden());
    }

    // --- Print a user by userId --> GET /api/users/{id} ---

    @Test
    void findUserById_returnsUser_whenClientSuper() throws Exception {
        when(userService.findUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_super"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ali"))
                .andExpect(jsonPath("$.email").value("miaoubich@example.com"));
    }

    @Test
    void findUserById_returnsUser_whenClientAdmin() throws Exception {
        when(userService.findUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_admin"))))
                .andExpect(status().isOk());
    }

    @Test
    void findUserById_returnsNotFound_whenUserDoesNotExist() throws Exception {
        when(userService.findUserById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/users/99")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_super"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with ID: 99"));
    }

    @Test
    void findUserById_returnsForbidden_whenClientUser() throws Exception {
        mockMvc.perform(get("/api/users/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_user"))))
                .andExpect(status().isForbidden());
    }

    // --- Edit user by userId --> PUT /api/users/{id} ---

    @Test
    void updateUser_returnsUpdatedUser_whenClientSuper() throws Exception {
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_super")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Bouzar"));
    }

    @Test
    void updateUser_returnsForbidden_whenClientUser() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isForbidden());
    }

    // --- update a user's pwd --> PATCH /api/users/{id}/password ---

    @Test
    void updateUserPassword_returnsOk_whenClientSuper() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setNewPassword("newSecret123");

        mockMvc.perform(patch("/api/users/1/password")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_super")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully"));

        verify(userService).updateUserPasswordByUserId(eq(1L), any(UpdatePasswordRequest.class));
    }

    @Test
    void updateUserPassword_returnsOk_whenClientAdmin() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setNewPassword("newSecret123");

        mockMvc.perform(patch("/api/users/1/password")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUserPassword_returnsForbidden_whenClientUser() throws Exception {
        mockMvc.perform(patch("/api/users/1/password")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdatePasswordRequest())))
                .andExpect(status().isForbidden());
    }

    // --- delete user by userId --> DELETE /api/users/{id} ---

    @Test
    void deleteUser_returnsNoContent_whenClientSuper() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_super"))))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_returnsForbidden_whenClientAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_client_admin"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_returnsUnauthorized_whenNoAuth() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }
}
