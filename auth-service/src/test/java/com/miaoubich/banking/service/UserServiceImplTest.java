package com.miaoubich.banking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.miaoubich.banking.domain.User;
import com.miaoubich.banking.domain.UserRole;
import com.miaoubich.banking.dto.UpdatePasswordRequest;
import com.miaoubich.banking.exception.UserNotFoundException;
import com.miaoubich.banking.repository.UserRepository;

/*
 * unit tests for UserServiceImpl using Mockito to mock dependencies and AssertJ for assertions.
 * */

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "realm", "test-realm");
        user = new User("Ali", "Bouzar", "miaoubich", "miaoubich@example.com", "0123456789", "kc-uuid-123", UserRole.client_user);
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    void findUserById_returnsUser_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findUserById(1L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findUserById_throwsUserNotFoundException_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAllUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.findAllUsers();

        assertThat(result).hasSize(1).contains(user);
    }

    @Test
    void updateUser_updatesFieldsAndSaves() {
        User updatedData = new User("Jane", "Smith", "janesmith", "jane@example.com", "0987654321", "kc-uuid-123", UserRole.client_user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(1L, updatedData);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getPhone()).isEqualTo("0987654321");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_throwsUserNotFoundException_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, user))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteUser_deletesFromKeycloakAndDb() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(keycloak.realm("test-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        userService.deleteUser(1L);

        verify(usersResource).delete("kc-uuid-123");
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_throwsUserNotFoundException_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verifyNoInteractions(keycloak);
    }

    @Test
    void updateUserPassword_resetsPasswordInKeycloak() {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setNewPassword("newSecret123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(keycloak.realm("test-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("kc-uuid-123")).thenReturn(userResource);

        userService.updateUserPasswordByUserId(1L, request);

        verify(userResource).resetPassword(argThat(cred ->
                cred.getValue().equals("newSecret123") &&
                cred.getType().equals(CredentialRepresentation.PASSWORD) &&
                !cred.isTemporary()
        ));
    }

    @Test
    void updateUserPassword_throwsUserNotFoundException_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserPasswordByUserId(99L, new UpdatePasswordRequest()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verifyNoInteractions(keycloak);
    }
}
