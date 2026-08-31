package com.path.inventory.security;

import com.path.inventory.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    private final LoginAuthenticationSuccessHandler
            loginAuthenticationSuccessHandler;


    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            LoginAuthenticationSuccessHandler
                    loginAuthenticationSuccessHandler) {

        this.userDetailsService =
                userDetailsService;

        this.loginAuthenticationSuccessHandler =
                loginAuthenticationSuccessHandler;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {


        http

                .csrf(csrf ->
                        csrf.disable()
                )


                .authenticationProvider(
                        authenticationProvider()
                )


                /*
                 * CAPTCHA is checked before
                 * Spring Security processes
                 * username/password.
                 */
                .addFilterBefore(
                        new CaptchaFilter(),
                        UsernamePasswordAuthenticationFilter.class
                )


                .authorizeHttpRequests(auth -> auth


                        /* =================================
                           PUBLIC PAGES
                           ================================= */

                        .requestMatchers(

                                "/login",

                                "/forgot-password",

                                "/verify-otp",

                                "/forgot-password-reset",

                                "/verify-login-otp",

                                "/resend-login-otp",

                                "/reset-password",

                                "/access-denied",

                                "/css/**",

                                "/js/**",

                                "/images/**"

                        )
                        .permitAll()


                        /* =================================
                           USER MANAGEMENT
                           ADMIN ONLY
                           ================================= */

                        .requestMatchers(
                                "/users/**"
                        )
                        .hasRole("ADMIN")


                        /* =================================
                           EMPLOYEE MODIFICATION
                           ADMIN ONLY
                           ================================= */

                        .requestMatchers(

                                "/employees/new",

                                "/employees/save",

                                "/employees/edit/**",

                                "/employees/update/**",

                                "/employees/delete/**"

                        )
                        .hasRole("ADMIN")


                        /* =================================
                           DEVICE MODIFICATION
                           ADMIN + CATEGORY USER
                           ================================= */

                        .requestMatchers(

                                "/devices/new",

                                "/devices/save",

                                "/devices/edit/**",

                                "/devices/update/**",

                                "/devices/delete/**",

                                "/assign-device/**"

                        )
                        .hasAnyRole(
                                "ADMIN",
                                "CATEGORY_USER"
                        )


                        /* =================================
                           VIEWING
                           ================================= */

                        .requestMatchers(

                                "/devices",

                                "/devices/**",

                                "/employees",

                                "/employees/**",

                                "/dashboard"

                        )
                        .hasAnyRole(

                                "ADMIN",

                                "VIEWER",

                                "CATEGORY_USER"

                        )


                        /* =================================
                           EVERYTHING ELSE
                           ================================= */

                        .anyRequest()
                        .authenticated()

                )


                /* =========================================
                   ACCESS DENIED
                   ========================================= */

                .exceptionHandling(exception ->

                        exception.accessDeniedPage(
                                "/access-denied"
                        )

                )


                /* =========================================
                   LOGIN
                   ========================================= */

                .formLogin(login ->

                        login

                                .loginPage(
                                        "/login"
                                )

                                .loginProcessingUrl(
                                        "/login"
                                )

                                /*
                                 * IMPORTANT:
                                 *
                                 * We no longer send the
                                 * user directly to dashboard.
                                 *
                                 * Successful username/password
                                 * authentication goes to our
                                 * OTP success handler.
                                 */

                                .successHandler(
                                        loginAuthenticationSuccessHandler
                                )

                                .failureUrl(
                                        "/login?error=true"
                                )

                                .permitAll()

                )


                /* =========================================
                   LOGOUT
                   ========================================= */

                .logout(logout ->

                        logout

                                .logoutUrl(
                                        "/logout"
                                )

                                .logoutSuccessUrl(
                                        "/login?logout=true"
                                )

                                .invalidateHttpSession(
                                        true
                                )

                                .deleteCookies(
                                        "JSESSIONID"
                                )

                                .permitAll()

                );


        return http.build();

    }

}