package mgmt.student.studentapi.config.jwt;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import mgmt.student.studentapi.config.AppConfig;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (isWhitelisted(path) || request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info(request.getMethod());
        log.info(HttpMethod.OPTIONS.name());

        String requestHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        try {
            if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
                token = requestHeader.substring(7);
                log.info("Token >>>>> {}", token);

                // Extract username from token
                username = tokenUtil.getUserNameFromToken(token);

                // Validate token and set authentication if valid
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (tokenUtil.validateToken(token, userDetails)) {
                        log.info("Token {} is valid", token);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Set authentication in the SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        log.error("Token validation failed");
                        handleException(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
                        return;
                    }
                }
            } else {
                log.warn("Authorization header is missing or doesn't start with Bearer");
                handleException(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization header is missing or invalid");
                return;
            }
        } catch (Exception e) {
            log.error("Exception during JWT processing", e);
            handleException(response, HttpServletResponse.SC_UNAUTHORIZED, "Internal error during JWT processing");
            return;
        }

        // Continue with the filter chain

        log.info("token valid, proceeding with request..." );
        log.info("SecurityContext after setting authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String requestPath) {
        List<String> stringList = Arrays.asList(AppConfig.AUTH_WHITELIST);
        return stringList.stream().anyMatch(path -> requestPath.matches(path.replace("**", ".*")));
    }

    private void handleException(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
