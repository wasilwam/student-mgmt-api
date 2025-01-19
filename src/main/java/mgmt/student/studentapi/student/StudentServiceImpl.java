package mgmt.student.studentapi.student;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentRespository studentRespository;

    @Resource
    private StudentMapper studentMapper;

    @Override
    public String uploadPhoto() {
        return "";
    }

    @Override
    public String getStudentsFiltered() {
        return "";
    }

    @Override
    public Student getStudent(BigInteger studentId) {
        return studentRespository.findById(studentId.longValue())
                .map(studentOR -> studentMapper.toApi(studentOR))
                .orElseThrow(() -> new RuntimeException("Student not found: Id " + studentId));
    }

    @Override
    public void deleteStudent(BigInteger studentId) {
        log.info("deleting user with id {}", studentId);
    }

    @Override
    public Student updateStudent(Student student) {
        log.info("updating student with DOB {}", student.getDob());
        return studentRespository.findById(student.getStudentId().longValue())
                .map(studentOR -> studentRespository
                        .save(studentMapper.toEntity(student))) // student exists, update the entry
                .map(studentOR -> studentMapper.toApi(studentOR)) // entry returned from saving. convert to DTO
                .orElseThrow(() -> new RuntimeException("Student not found: Id " + student.getStudentId()));
    }

    @Override
    public List<Student> getStudents() {
        return studentRespository.findAll().stream()
                .map(studentOR -> studentMapper.toApi(studentOR))
                .collect(Collectors.toList());
    }

    @Override
    public long getStudentsCount() {
        return studentRespository.count();
    }
}
