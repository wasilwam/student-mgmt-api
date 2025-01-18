package mgmt.student.studentapi.student;

import lombok.*;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    BigInteger studentId;
    private String firstName;
    private String lastName;
    private String DOB;
    private String studentClass;
    private String score;
    private String photoPath;
    private Integer status;
}
