package mgmt.student.studentapi.student;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    @Value("${file.photo-path-url-prefix}")
    private String photoUrlPrefix;

    @Override
    public Student uploadPhoto(MultipartFile file, BigInteger studentId) {
        String filename = UUID.randomUUID().toString().replace("-", "");
        String fileExt = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf("."));
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
                var photoUrl = photoUrlPrefix + "/students/file/" + filename + fileExt;
                s.setPhotoPath(photoUrl);
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

    public org.springframework.core.io.Resource getPhoto(String filename) {
        try {
            Path path = Paths.get(studentPhotoBasePath).toAbsolutePath().normalize();
            Path filePath = path.resolve(filename);
            org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + filename, e);
        }
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
    public Page<Student> getStudents(int page, int size) {
        // List<Student> students = studentRespository.findAll().stream()
        // .map(studentOR -> studentMapper.toApi(studentOR))
        // .collect(Collectors.toList());

        // Ignore sorting for now
        // Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
        // ? Sort.by(sortBy).ascending()
        // : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size);

        Page<StudentOR> studentPage = studentRespository.findAll(pageable);

        List<Student> students = studentPage.getContent()
                .stream()
                .map(studentMapper::toApi)
                .collect(Collectors.toList());

        return new PageImpl<>(students, pageable, studentPage.getTotalElements());
    }

    @Override
    public long getStudentsCount() {
        return studentRespository.count();
    }
}
