package mgmt.student.studentapi.student;

import jakarta.persistence.*;
import lombok.*;
import mgmt.student.studentapi.entity.BaseEntity;

import java.math.BigInteger;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentOR extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id", nullable = false, updatable = false)
    private BigInteger studentId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "dob", nullable = false)
    private String DOB;

    @Column(name = "student_class", nullable = false, length = 50)
    private String studentClass;

    @Column(name = "score", nullable = false)
    private String score;

    @Column(name = "photo_path")
    private String photoPath;

    @Column(name = "status", nullable = false)
    private Integer status;
}