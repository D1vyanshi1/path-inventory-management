package com.path.inventory.security;

import com.path.inventory.entity.User;
import com.path.inventory.repository.UserRepository;
import com.path.inventory.service.EmailService;
import com.path.inventory.service.OtpService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EmailService emailService;

    public LoginAuthenticationSuccessHandler(
            UserRepository userRepository,
            OtpService otpService,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String username =
                authentication.getName();

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );

        String email =
                user.getEmail();

        if (email == null || email.isBlank()) {

            response.sendRedirect(
                    "/login?emailError=true"
            );

            return;
        }

        /*
         * Generate login OTP
         */
        String otp =
                otpService.generateLoginOtp(email);

        /*
         * Send OTP to registered email
         */
        emailService.sendEmail(
                email,
                "PATH Inventory Login OTP",
                "Your login OTP is: "
                        + otp
                        + "\n\n"
                        + "This OTP is valid for 5 minutes."
                        + "\n\n"
                        + "If you did not attempt to log in, "
                        + "please ignore this email."
        );

        /*
         * Remember the authenticated user
         * temporarily in the session.
         */
        request.getSession().setAttribute(
                "loginOtpEmail",
                email
        );

        request.getSession().setAttribute(
                "loginOtpUsername",
                username
        );

        /*
         * Go to OTP verification page.
         */
        response.sendRedirect(
                "/verify-login-otp"
        );
    }
}