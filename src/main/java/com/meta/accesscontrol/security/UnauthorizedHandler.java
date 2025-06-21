package com.meta.accesscontrol.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.accesscontrol.controller.payload.response.JsonResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class UnauthorizedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        JsonResponse<Void> body = new JsonResponse<>(403, "You do not have permission to access this resource", null);
        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
} 