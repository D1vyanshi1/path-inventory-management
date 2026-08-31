package com.path.inventory.repository;

import com.path.inventory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    //Optional<User> findByUsernameAndEmail(String username, String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

}