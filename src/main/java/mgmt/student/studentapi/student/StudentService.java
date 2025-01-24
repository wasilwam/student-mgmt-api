package mgmt.student.studentapi.student;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;

public interface StudentService {
     Page<Student> getStudents(int page, int size);

     long getStudentsCount();

     Student updateStudent(Student student);

     void deleteStudent(BigInteger studentId);

     Student getStudent(BigInteger studentId);

     Student uploadPhoto(MultipartFile file, BigInteger studentId);

     Resource getPhoto(String filename);
}
