package mgmt.student.studentapi.student;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    BigInteger studentId;
    private String firstName;
    private String lastName;
    private String dob;
    @JsonProperty("class")
    private String studentClass;
    private String score;
    private String photoPath;
    private Integer status;
}
