package com.path.inventory;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordCheck {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String hash = "$2a$10$Pw9wojnsh6hnZWU/hif/IuM0mEobLm.wicr4kGgFc292367Ay/kpS";

        System.out.println(encoder.matches("admin123", hash));
    }
}