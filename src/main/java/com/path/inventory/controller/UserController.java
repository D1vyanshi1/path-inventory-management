package com.path.inventory.controller;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.path.inventory.entity.User;
import com.path.inventory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public String listUsers(Model model) {

        List<User> users = userRepository.findAll();

        model.addAttribute("users", users);

        return "users";
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users/add")
    public String showAddUserForm(Model model) {

        model.addAttribute("user", new User());

        return "add-user";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid User Id"));

        model.addAttribute("user", user);

        return "edit-user";
    }

    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute("user") User user,
                             Model model) {

        if (userRepository.existsByEmailAndIdNot(user.getEmail(), user.getId())) {

            model.addAttribute("error", "Email already exists.");
            model.addAttribute("user", user);

            return "edit-user";
        }

        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid User Id"));

        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setRole(user.getRole());
        existingUser.setEnabled(user.isEnabled());

        if ("ROLE_CATEGORY_USER".equals(user.getRole())) {
            existingUser.setAssetCategory(user.getAssetCategory());
        } else {
            existingUser.setAssetCategory(null);
        }

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userRepository.save(existingUser);

        return "redirect:/users";
    }

    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute("user") User user,
                           Model model) {

        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email already exists.");
            model.addAttribute("user", user);
            return "add-user";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if ("ROLE_CATEGORY_USER".equals(user.getRole())) {
            // Category is required for category-based users
            if (user.getAssetCategory() == null ||
                    user.getAssetCategory().isBlank()) {

                model.addAttribute("error",
                        "Please select an asset category.");

                model.addAttribute("user", user);

                return "add-user";
            }
        } else {
            // Admin and Viewer don't need an asset category
            user.setAssetCategory(null);
        }

        if (!user.isEnabled()) {
            user.setEnabled(false);
        }

        long count = userRepository.count() + 1;
        user.setUserId(String.format("USR-%04d", count));

        userRepository.save(user);

        return "redirect:/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {

        userRepository.deleteById(id);

        return "redirect:/users";
    }

}