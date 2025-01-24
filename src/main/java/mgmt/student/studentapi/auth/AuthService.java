package mgmt.student.studentapi.auth;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import mgmt.student.studentapi.config.jwt.TokenUtil;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private TokenUtil tokenUtil;

    public ResponseEntity<TokenResponse> getToken(TokenRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                request.getUsername(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // UserDetails user = (UserDetails) authentication.getPrincipal();
        UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());
        String jwtToken = tokenUtil.generateToken(user.getUsername());
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        TokenResponse jwtResponse = new TokenResponse();
        jwtResponse.setUsername(user.getUsername());
        jwtResponse.setRoles(roles);
        jwtResponse.setBearerToken(jwtToken);
        jwtResponse.setExpiresIn(TokenUtil.JWT_TOKEN_VALIDITY);

        return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
    }
}
