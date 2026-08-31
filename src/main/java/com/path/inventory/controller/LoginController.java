package com.path.inventory.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.path.inventory.entity.User;
import com.path.inventory.repository.UserRepository;
import com.path.inventory.service.EmailService;
import com.path.inventory.service.OtpService;
import com.path.inventory.util.CaptchaUtil;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserDetailsService userDetailsService;


    /* =====================================================
       LOGIN PAGE
       ===================================================== */

    @GetMapping("/login")
    public String login(
            Model model,
            HttpSession session) {

        System.out.println(
                "Session ID : " + session.getId()
        );

        String captcha =
                (String) session.getAttribute("captcha");

        if (captcha == null) {

            captcha =
                    CaptchaUtil.generateCaptcha();

            session.setAttribute(
                    "captcha",
                    captcha
            );
        }

        model.addAttribute(
                "captcha",
                captcha
        );

        return "login";
    }


    /* =====================================================
       LOGIN OTP PAGE
       ===================================================== */

    @GetMapping("/verify-login-otp")
    public String showLoginOtpPage(
            HttpSession session,
            Model model) {

        String email =
                (String) session.getAttribute(
                        "loginOtpEmail"
                );

        if (email == null) {

            return "redirect:/login";

        }

        model.addAttribute(
                "email",
                maskEmail(email)
        );

        return "verify-login-otp";
    }


    /* =====================================================
       VERIFY LOGIN OTP
       ===================================================== */

    @PostMapping("/verify-login-otp")
    public String verifyLoginOtp(
            @RequestParam String otp,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {


        String email =
                (String) session.getAttribute(
                        "loginOtpEmail"
                );


        String username =
                (String) session.getAttribute(
                        "loginOtpUsername"
                );


        if (email == null || username == null) {

            return "redirect:/login";

        }


        /* =========================================
           VERIFY OTP
           ========================================= */

        boolean valid =
                otpService.verifyLoginOtp(
                        email,
                        otp
                );


        if (!valid) {

            model.addAttribute(
                    "email",
                    maskEmail(email)
            );

            model.addAttribute(
                    "error",
                    "Invalid or expired OTP."
            );

            return "verify-login-otp";
        }


        /* =========================================
           OTP IS CORRECT
           ========================================= */

        UserDetails userDetails =
                userDetailsService
                        .loadUserByUsername(username);


        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );


        SecurityContext securityContext =
                SecurityContextHolder
                        .createEmptyContext();


        securityContext.setAuthentication(
                authentication
        );


        SecurityContextHolder.setContext(
                securityContext
        );


        /*
         * Save authentication into the HTTP session.
         *
         * This is important because otherwise
         * Spring Security would forget the login
         * after redirecting to the dashboard.
         */

        SecurityContextRepository
                securityContextRepository =
                new HttpSessionSecurityContextRepository();


        securityContextRepository.saveContext(
                securityContext,
                request,
                response
        );


        /* =========================================
           CLEAN UP LOGIN OTP SESSION DATA
           ========================================= */

        session.removeAttribute(
                "loginOtpEmail"
        );

        session.removeAttribute(
                "loginOtpUsername"
        );


        /* =========================================
           GO TO DASHBOARD
           ========================================= */

        return "redirect:/dashboard";
    }


    /* =====================================================
       RESEND LOGIN OTP
       ===================================================== */

    @PostMapping("/resend-login-otp")
    public String resendLoginOtp(
            HttpSession session,
            Model model) {


        String email =
                (String) session.getAttribute(
                        "loginOtpEmail"
                );


        String username =
                (String) session.getAttribute(
                        "loginOtpUsername"
                );


        if (email == null || username == null) {

            return "redirect:/login";

        }


        /* =========================================
           GENERATE NEW OTP
           ========================================= */

        String otp =
                otpService.generateLoginOtp(
                        email
                );


        /* =========================================
           SEND NEW OTP
           ========================================= */

        emailService.sendEmail(

                email,

                "PATH Inventory Login OTP",

                "Your new login OTP is: "
                        + otp
                        + "\n\n"
                        + "This OTP is valid for 5 minutes."
                        + "\n\n"
                        + "If you did not attempt to log in, "
                        + "please ignore this email."
        );


        model.addAttribute(
                "email",
                maskEmail(email)
        );


        model.addAttribute(
                "success",
                "A new OTP has been sent to your registered email."
        );


        return "verify-login-otp";
    }


    /* =====================================================
       MASK EMAIL
       ===================================================== */

    private String maskEmail(String email) {

        if (email == null ||
                !email.contains("@")) {

            return email;
        }


        String[] parts =
                email.split("@");


        String username =
                parts[0];

        String domain =
                parts[1];


        if (username.length() <= 2) {

            return username.charAt(0)
                    + "***@"
                    + domain;
        }


        return username.substring(0, 2)
                + "***@"
                + domain;
    }


    /* =====================================================
       EXISTING RESET PASSWORD
       ===================================================== */

    @GetMapping("/reset-password")
    public String showResetPasswordPage() {

        return "reset-password";
    }


    @PostMapping("/reset-password")
    public String resetPassword(

            @RequestParam String username,

            @RequestParam String currentPassword,

            @RequestParam String newPassword,

            @RequestParam String confirmPassword,

            Model model) {


        Optional<User> optionalUser =
                userRepository.findByUsername(
                        username
                );


        if (optionalUser.isEmpty()) {

            model.addAttribute(
                    "error",
                    "Username not found."
            );

            return "reset-password";
        }


        if (!newPassword.equals(
                confirmPassword)) {

            model.addAttribute(
                    "error",
                    "Passwords do not match."
            );

            return "reset-password";
        }


        User user =
                optionalUser.get();


        if (!passwordEncoder.matches(

                currentPassword,

                user.getPassword()

        )) {

            model.addAttribute(
                    "error",
                    "Current password is incorrect."
            );

            return "reset-password";
        }


        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );


        userRepository.save(user);


        model.addAttribute(
                "success",
                "Password updated successfully."
        );


        return "reset-password";
    }


    /* =====================================================
       TEST ENDPOINT
       ===================================================== */

    @GetMapping("/test")
    public String test() {

        String hash =
                userRepository
                        .findByUsername("divyanshi")
                        .get()
                        .getPassword();


        System.out.println(
                "Matches dv123 = "
                        + passwordEncoder.matches(
                        "dv123",
                        hash
                )
        );


        System.out.println(
                "Matches d123 = "
                        + passwordEncoder.matches(
                        "d123",
                        hash
                )
        );


        return "login";
    }


    /* =====================================================
       TEST EMAIL
       ===================================================== */

    @GetMapping("/test-email")
    @ResponseBody
    public String testEmail() {

        emailService.sendEmail(

                "divyanshii2704@gmail.com",

                "PATH Inventory Test",

                "Congratulations! Your email configuration "
                        + "is working."

        );


        return "Email Sent Successfully!";
    }


    /* =====================================================
       FORGOT PASSWORD
       ===================================================== */

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {

        return "forgot-password";
    }


    @PostMapping("/forgot-password")
    public String sendOtp(

            @RequestParam String username,

            @RequestParam String email,

            Model model) {


        Optional<User> optionalUser =
                userRepository.findByUsername(
                        username
                );


        if (optionalUser.isEmpty()) {

            model.addAttribute(
                    "error",
                    "Username not found."
            );

            return "forgot-password";
        }


        User user =
                optionalUser.get();


        if (!user.getEmail()
                .equalsIgnoreCase(email)) {

            model.addAttribute(
                    "error",
                    "Username and email do not match."
            );

            return "forgot-password";
        }


        String otp =
                otpService.generateOtp(
                        email
                );


        emailService.sendEmail(

                email,

                "PATH Inventory Password Reset OTP",

                "Your OTP is: " + otp

        );


        model.addAttribute(
                "email",
                email
        );


        return "verify-otp";
    }


    /* =====================================================
       VERIFY FORGOT PASSWORD OTP
       ===================================================== */

    @PostMapping("/verify-otp")
    public String verifyOtp(

            @RequestParam String email,

            @RequestParam String otp,

            Model model) {


        if (!otpService.verifyOtp(
                email,
                otp
        )) {

            model.addAttribute(
                    "email",
                    email
            );

            model.addAttribute(
                    "error",
                    "Invalid or Expired OTP."
            );

            return "verify-otp";
        }


        model.addAttribute(
                "email",
                email
        );


        return "forgot-password-reset";
    }


    /* =====================================================
       FORGOT PASSWORD RESET
       ===================================================== */

    @PostMapping("/forgot-password-reset")
    public String forgotPasswordReset(

            @RequestParam String email,

            @RequestParam String newPassword,

            @RequestParam String confirmPassword,

            Model model) {


        if (!newPassword.equals(
                confirmPassword)) {

            model.addAttribute(
                    "email",
                    email
            );

            model.addAttribute(
                    "error",
                    "Passwords do not match."
            );

            return "forgot-password-reset";
        }


        Optional<User> optionalUser =
                userRepository.findByEmail(
                        email
                );


        if (optionalUser.isEmpty()) {

            model.addAttribute(
                    "error",
                    "User not found."
            );

            return "forgot-password";
        }


        User user =
                optionalUser.get();


        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );


        userRepository.save(user);


        otpService.removeOtp(
                email
        );


        return "redirect:/login?resetSuccess";
    }

}