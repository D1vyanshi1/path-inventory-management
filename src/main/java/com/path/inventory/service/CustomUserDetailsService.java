package com.path.inventory.service;

import com.path.inventory.entity.User;
import com.path.inventory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        System.out.println("Loading user: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        System.out.println("DB Username : " + user.getUsername());
        System.out.println("DB Password : " + user.getPassword());
        System.out.println("DB Role     : " + user.getRole());
        System.out.println("Enabled     : " + user.isEnabled());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole())
                .disabled(!user.isEnabled())
                .build();
    }
}