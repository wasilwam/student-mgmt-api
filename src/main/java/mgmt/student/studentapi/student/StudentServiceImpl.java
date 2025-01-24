package mgmt.student.studentapi.student;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private static final String APPROVAL_STATUS_PENDING = "Pending";
    private static final String APPROVAL_STATUS_APPROVED = "Approved";
    private static final String APPROVAL_STATUS_REJECTED = "Rejected";

    public static final String APPROVAL_ACTION_APPROVE = "Approve";

    public static final String APPROVAL_ACTION_REJECT = "Reject";

    public static final String APPROVAL_STEP_MAKER = "Maker";

    public static final String APPROVAL_STEP_CHECKER = "Checker";
    public static final String APPROVAL_STEP_COMPLETE = "Complete";

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
    public Student updateStudent(BigInteger studentId, Student student) {
        // throw to avoid create/update hibernate scenario
        boolean studentExists = studentRespository.existsById(studentId.longValue());
        if (!studentExists) {
            throw new RuntimeException("Student not found: Id " + student.getStudentId());
        }

        student.setStudentId(studentId);
        log.info("MAKER updating student {}", student.getStudentId());
        Student updatedStudent = studentRespository.findById(student.getStudentId().longValue())
                .map(studentOR -> studentRespository.save(studentMapper.toEntity(student)))
                .map(studentOR -> {
                    studentOR.setApprovalStep(APPROVAL_STEP_CHECKER);
                    studentOR.setApprovalStatus(APPROVAL_STATUS_PENDING);
                    return studentRespository.save(studentOR);
                })
                .map(studentOR -> studentMapper.toApi(studentOR))
                .orElseThrow(() -> new RuntimeException("Student not found: Id " + student.getStudentId()));
        return updatedStudent;
    }

    @Override
    public Student approvalChecker(BigInteger studentId, String action, String comment) {
        // throw to avoid create/update hibernate scenario
        boolean studentExists = studentRespository.existsById(studentId.longValue());
        if (!studentExists) {
            throw new RuntimeException("Student not found: Id " + studentId);
        }

        log.info("approving CHECKER for student {}", studentId);
        Optional<StudentOR> byId = studentRespository.findById(studentId.longValue());
        StudentOR studentOR = byId.get();
        if (null == studentOR.getApprovalStatus() || !studentOR.getApprovalStep().equals(APPROVAL_STEP_CHECKER)) {
            throw new RuntimeException("bad request: student is not in approval step " + APPROVAL_STEP_CHECKER);
        }
        if (action.equals(APPROVAL_ACTION_APPROVE)) {
            studentOR.setApprovalStep(APPROVAL_STEP_COMPLETE);
            studentOR.setApprovalStatus(APPROVAL_STATUS_APPROVED);
        } else if (action.equals(APPROVAL_ACTION_REJECT)) {
            studentOR.setApprovalStep(APPROVAL_STEP_MAKER);
            studentOR.setApprovalStatus(APPROVAL_STATUS_REJECTED);
            if (Objects.isNull(comment)) {
                throw new RuntimeException("bad request: comment is required for rejection");
            }
            studentOR.setRejectionComment(comment);
        } else {
            throw new RuntimeException("bad request: unknown approval action " + action);
        }
        return studentMapper.toApi(studentRespository.save(studentOR));
    }

    @Override
    public Page<Student> getStudents(int page, int size) {
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
    public List<Student> getStudents() {
        log.info("fetching all students");
        return studentRespository.findAll().stream()
                .map(studentMapper::toApi)
                .collect(Collectors.toList());
    }

    @Override
    public long getStudentsCount() {
        return studentRespository.count();
    }
}
