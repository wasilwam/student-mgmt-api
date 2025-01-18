package mgmt.student.studentapi.student;

import java.math.BigInteger;

public interface StudentService {
     String getStudents();

     String updateStudent();

     String deleteStudent(BigInteger studentId);

     String getStudent(BigInteger studentId);

     String getStudentsFiltered();

     String uploadPhoto();
}
