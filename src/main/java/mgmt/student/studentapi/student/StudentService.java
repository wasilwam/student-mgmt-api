package mgmt.student.studentapi.student;

import java.math.BigInteger;
import java.util.List;

public interface StudentService {
     List<Student> getStudents();
     long getStudentsCount();
     Student updateStudent(Student student);

     void deleteStudent(BigInteger studentId);

     Student getStudent(BigInteger studentId);

     String getStudentsFiltered();

     String uploadPhoto();
}
