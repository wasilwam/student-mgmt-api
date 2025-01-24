package mgmt.student.studentapi.student;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.List;

public interface StudentService {
     Page<Student> getStudents(int page, int size);

     List<Student> getStudents();

     Student getStudent(BigInteger studentId);

     long getStudentsCount();

     Student updateStudent(BigInteger studentId, Student student);

     Student approvalChecker(BigInteger studentId, String action, String comment);

     void deleteStudent(BigInteger studentId);

     Student uploadPhoto(MultipartFile file, BigInteger studentId);

     Resource getPhoto(String filename);
}
