package mgmt.student.studentapi.excel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;

public interface ExcelRepository extends JpaRepository<ExcelOR, BigInteger> {
    @Query("SELECT e FROM ExcelOR e ORDER BY e.fileId DESC LIMIT 1")
    ExcelOR findLast();
}