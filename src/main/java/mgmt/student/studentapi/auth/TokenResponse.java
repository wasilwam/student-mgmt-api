package mgmt.student.studentapi.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenResponse {
    private String bearerToken;
    private String username;
    private String expiresIn;
}
