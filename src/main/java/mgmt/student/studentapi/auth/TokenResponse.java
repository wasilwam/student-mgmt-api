package mgmt.student.studentapi.auth;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenResponse {
    private String bearerToken;
    private String username;
    private List<String> roles;
    private long expiresIn;
}
