package mgmt.student.studentapi.auth;

import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping(value = "/auth/signin", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TokenResponse> signin(@RequestBody TokenRequest r) {
        return authService.getToken(r);
    }
}