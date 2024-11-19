package org.example.postproject.config;

import lombok.RequiredArgsConstructor;
import org.example.postproject.entities.user.UserEntity;
import org.example.postproject.exception.DataNotFoundException;
import org.example.postproject.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserEntity authUser = authUserRepository.findByEmail(username)
                .orElseThrow(() -> new DataNotFoundException("User not found with email: " + username));

        return authUser;
    }
}
