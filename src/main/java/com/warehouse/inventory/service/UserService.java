package com.warehouse.inventory.service;

import com.warehouse.inventory.entity.User;
import com.warehouse.inventory.enums.UserRole;
import com.warehouse.inventory.exception.DuplicateResourceException;
import com.warehouse.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

    // TODO: optimize this section for better performance
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User createUser(String username, String password, String fullName, String email, UserRole role) {
        log.info("Creating new user: {}", username);

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Người dùng", "tên đăng nhập", username);
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .email(email)
                .role(role)
                .active(true)
                .build();

        return userRepository.save(user);
    }
}
