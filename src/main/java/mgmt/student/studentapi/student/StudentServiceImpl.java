package mgmt.student.studentapi.student;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentRespository studentRespository;

    @Resource
    private StudentMapper studentMapper;

    @Value("${file.student-photo-base-path}")
    private String studentPhotoBasePath;

    @Override
    public Student uploadPhoto(MultipartFile file, BigInteger studentId) {
        String filename = UUID.randomUUID().toString().replace("-", "");
        String fileExt = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
        String photoAbsPath = studentPhotoBasePath + filename + fileExt;
        log.info("Uploading file {} to {}", filename, photoAbsPath);
        try {

            Path exportPath = Path.of(studentPhotoBasePath);
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
            }

            file.transferTo(Path.of(photoAbsPath));
            log.info("file saved to {}", photoAbsPath);
            Optional<StudentOR> student = studentRespository.findById(Long.valueOf(studentId.toString()));
            if (student.isPresent()) {
                var s = student.get();
                s.setPhotoPath(photoAbsPath);
                studentRespository.save(s);
                log.info("updated student photo_url = {}", photoAbsPath);
                return studentMapper.toApi(s);
            } else {
                log.info("student not found, removing file {}", photoAbsPath);
                Files.deleteIfExists(Path.of(photoAbsPath));
                throw new RuntimeException("Student not found");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        studentRespository.deleteById(studentId.longValue());
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
