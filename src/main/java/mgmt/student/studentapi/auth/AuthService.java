package mgmt.student.studentapi.auth;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import mgmt.student.studentapi.config.jwt.TokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    public ResponseEntity<TokenResponse> getToken(TokenRequest jwtRequest){
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(jwtRequest.getUsername(),jwtRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //UserDetails user = (UserDetails) authentication.getPrincipal();
        UserDetails user = userDetailsService.loadUserByUsername(jwtRequest.getUsername());
        String jwtToken = tokenUtil.generateToken(user.getUsername());
        TokenResponse jwtResponse = new TokenResponse();
        jwtResponse.setUsername(user.getUsername());
        jwtResponse.setBearerToken(jwtToken);

        // TODO: Remove token, security violation
        log.info(jwtToken);
        return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
    }
}
