package com.meetpick.auth.service;

import com.meetpick.auth.dto.request.SignupRequest;
import com.meetpick.auth.dto.response.SignupResponse;
import com.meetpick.global.error.BusinessException;
import com.meetpick.global.error.ErrorCode;
import com.meetpick.user.domain.User;
import com.meetpick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateDuplicateEmail(request.email());

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.create(
                request.email(),
                encodedPassword,
                request.nickname()
        );

        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
}