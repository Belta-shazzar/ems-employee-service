package com.ems.employeeservice.config.security;

import com.ems.employeeservice.config.security.enums.UserHttpHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserInfoExtractionFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
          HttpServletRequest request,
          HttpServletResponse response,
          FilterChain filterChain
  ) throws ServletException, IOException {
    final String email = request.getHeader(UserHttpHeaders.X_EMAIL);
    final String employeeId = request.getHeader(UserHttpHeaders.X_EMPLOYEE_ID);
    final String rolesHeader = request.getHeader(UserHttpHeaders.X_EMPLOYEE_ROLE);

    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      List<SimpleGrantedAuthority> authorities = List.of();

      if (rolesHeader != null && !rolesHeader.isEmpty()) {
        authorities = Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
      }

      // Build a lightweight UserDetails without hitting the DB
      User userDetails = new User(email, "", authorities);

      UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      // Optionally attach the employeeId for later retrieval
      authentication.setDetails(employeeId);

      SecurityContextHolder.getContext().setAuthentication(authentication);
      log.debug("Authenticated user from headers: {} with roles {}", email, rolesHeader);
    }

    filterChain.doFilter(request, response);
  }
}
