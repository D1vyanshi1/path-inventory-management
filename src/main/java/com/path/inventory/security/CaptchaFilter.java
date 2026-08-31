package com.path.inventory.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CaptchaFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getServletPath().equals("/login")
                && request.getMethod().equalsIgnoreCase("POST")) {

            HttpSession session = request.getSession();

            System.out.println("Session ID : " + session.getId());

            String generatedCaptcha =
                    (String) session.getAttribute("captcha");

            String userCaptcha =
                    request.getParameter("captchaInput");

            System.out.println("Session Captcha : " + generatedCaptcha);
            System.out.println("User Captcha    : " + userCaptcha);

            if (generatedCaptcha == null
                    || userCaptcha == null
                    || !generatedCaptcha.equalsIgnoreCase(userCaptcha)) {

                response.sendRedirect("/login?captchaError=true");
                return;
            }

            // Remove the captcha after successful validation
            session.removeAttribute("captcha");
        }

        filterChain.doFilter(request, response);
    }
}