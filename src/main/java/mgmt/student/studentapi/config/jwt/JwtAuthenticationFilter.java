package mgmt.student.studentapi.config.jwt;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mgmt.student.studentapi.config.AppConfig;
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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private UserDetailsService userDetailsService ;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull  FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (isWhitelisted(path)) {
            filterChain.doFilter(request, response); // Skip JWT validation
            return;
        }
        String requestHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;
        try{
            if(requestHeader != null && requestHeader.startsWith("Bearer")){
                token = requestHeader.substring(7);
                username = tokenUtil.getUserNameFromToken(token);
                if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    Boolean validateToken = tokenUtil.validateToken(token,userDetails);
                    if(validateToken){
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                    else {
                        System.out.println("Validation Failed");
                    }
                }
                filterChain.doFilter(request,response);
            }
            else{
                handleException(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT Token is empty or null");
            }
        } catch (Exception e) {
            System.out.println("Exception : "+e);
            handleException(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
        }
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
