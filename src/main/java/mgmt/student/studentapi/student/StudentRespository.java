package mgmt.student.studentapi.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRespository extends JpaRepository<StudentOR, Long> {
    Page<StudentOR> findAll(Pageable pageable);
}
