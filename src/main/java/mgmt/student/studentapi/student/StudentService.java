package mgmt.student.studentapi.student;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.List;

public interface StudentService {
     List<Student> getStudents();
     long getStudentsCount();
     Student updateStudent(Student student);

     void deleteStudent(BigInteger studentId);

     Student getStudent(BigInteger studentId);

     String getStudentsFiltered();

     Student uploadPhoto(MultipartFile file, BigInteger studentId);
}
