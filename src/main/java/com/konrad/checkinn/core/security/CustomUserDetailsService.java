package com.konrad.checkinn.core.security;

import com.konrad.checkinn.core.entity.User;
import com.konrad.checkinn.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService  implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        Optional<User> user =  userRepository.findUserByEmail(username);
        return user.orElseThrow(() -> new UsernameNotFoundException("No user found by the name of" + username));
    }
}
