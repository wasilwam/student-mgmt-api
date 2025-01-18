package mgmt.student.studentapi.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenRequest {
    private String username;
    private String password;
}
