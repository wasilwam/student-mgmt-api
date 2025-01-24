package mgmt.student.studentapi.student;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import mgmt.student.studentapi.entity.BaseEntity;

import java.math.BigInteger;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
    private String dob;

    @Column(name = "student_class", nullable = false, length = 50)
    private String studentClass;

    @Column(name = "score", nullable = false)
    private String score;

    @Column(name = "photo_path")
    private String photoPath;

    @Column(name = "approval_step") // Maker or Checker
    private String approvalStep;

    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "rejection_comment")
    private String rejectionComment;

    @Column(name = "status", nullable = false)
    private Integer status;

    public StudentOR(String firstName, String lastName, String dob, String studentClass, String score, String photoPath,
            Integer status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.studentClass = studentClass;
        this.score = score;
        this.photoPath = photoPath;
        this.status = status;
    }
}