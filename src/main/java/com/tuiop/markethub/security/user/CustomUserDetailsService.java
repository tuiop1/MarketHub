package com.tuiop.markethub.security.user;


import com.tuiop.markethub.users.User;
import com.tuiop.markethub.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: "+ email));

        return new CustomUserDetails(user);
    }

    public UserDetails loadUserById(UUID id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id ));

        return new CustomUserDetails(user);
    }
}
