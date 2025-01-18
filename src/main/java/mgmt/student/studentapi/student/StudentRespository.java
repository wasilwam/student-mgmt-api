package mgmt.student.studentapi.student;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRespository extends JpaRepository<StudentOR, Long> {
}
