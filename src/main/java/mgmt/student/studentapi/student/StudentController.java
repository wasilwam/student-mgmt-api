package mgmt.student.studentapi.student;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class StudentController {

    @Resource
    private StudentService studentService;

    @GetMapping("/students")
    private Page<Student> getStudents(@RequestParam("page") int page, @RequestParam("size") int size) {
        log.info("fetching paged students");
        return studentService.getStudents(page, size);
    }

    @GetMapping("/students/all")
    private List<Student> getStudents() {
        log.info("fetching paged students");
        return studentService.getStudents();
    }

    @GetMapping("/students/count")
    private Map<String, Long> getStudentsCount() {
        log.info("fetching students count");
        return Map.of("count", studentService.getStudentsCount());
    }

    @PreAuthorize("STUDENT_MAKER, ADMIN")
    @PutMapping("/students/{id}")
    private Student updateStudent(@RequestBody Student student, @PathVariable("id") BigInteger studentId) {
        log.info("updating student {}", student);
        return studentService.updateStudent(studentId, student);
    }

    @PreAuthorize("STUDENT_CHECKER, ADMIN")
    @PostMapping("/students/{id}/approval")
    private Student approveStudents(
            @PathVariable("id") BigInteger studentId,   
            @RequestParam String action,
            @RequestParam(required = false) String comment) {
        return studentService.approvalChecker(studentId, action, comment);
    }

    @DeleteMapping("/students/{id}")
    private ResponseEntity<Object> deleteStudent(@PathVariable("id") BigInteger studentId) {
        log.info("deleting student {}", studentId);
        studentService.deleteStudent(studentId);
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/students/{id}")
    private Student getStudent(@PathVariable("id") BigInteger studentId) {
        log.info("fetching student {}", studentId);
        return studentService.getStudent(studentId);
    }

    @PostMapping(path = "/students/{id}/upload-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Object> uploadPhoto(
            @PathVariable("id") BigInteger studentId,
            @RequestParam("file") MultipartFile file) {
        log.info("uploading photo...");
        return ResponseEntity.ok().body(studentService.uploadPhoto(file, studentId));
    }

    @GetMapping(path = "/students/file/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<org.springframework.core.io.Resource> downloadPhoto(
            @PathVariable("filename") String filename) {

        log.info("Downloading photo with filename: {}", filename);

        org.springframework.core.io.Resource resource = studentService.getPhoto(filename);
        if (null == resource) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
