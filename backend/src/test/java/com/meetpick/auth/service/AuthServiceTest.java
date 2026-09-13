package com.meetpick.auth.service;

import com.meetpick.auth.dto.request.SignupRequest;
import com.meetpick.auth.dto.response.SignupResponse;
import com.meetpick.global.error.BusinessException;
import com.meetpick.global.error.ErrorCode;
import com.meetpick.user.domain.User;
import com.meetpick.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void signupEncodesPasswordAndReturnsCreatedUser() {
        SignupRequest request = new SignupRequest(
                "user@example.com",
                "password123!",
                "meetpick"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        User savedUser = org.mockito.Mockito.mock(User.class);
        when(savedUser.getId()).thenReturn(1L);
        when(savedUser.getEmail()).thenReturn(request.email());
        when(savedUser.getNickname()).thenReturn(request.nickname());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        SignupResponse response = authService.signup(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User userToSave = userCaptor.getValue();

        assertThat(userToSave.getPassword())
                .isEqualTo("encoded-password")
                .isNotEqualTo(request.password());

        assertThat(response).isEqualTo(
                new SignupResponse(1L, "user@example.com", "meetpick")
        );
    }

    @Test
    void signupRejectsDuplicateEmail() {
        SignupRequest request = new SignupRequest(
                "duplicate@example.com",
                "password123!",
                "meetpick"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.signup(request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }
}