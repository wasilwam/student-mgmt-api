package mgmt.student.studentapi.student;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
    private List<Student> getStudents() {
        log.info("fetching all students");
        return studentService.getStudents();
    }

    @GetMapping("/students/count")
    private Map<String, Long> getStudentsCount() {
        log.info("fetching students count");
        return Map.of("count", studentService.getStudentsCount());
    }

    @PutMapping("/students/{id}")
    private Student updateStudent(@RequestBody Student student, @PathVariable("id") BigInteger studentId) {
        log.info("updating student {}", student);
        return studentService.updateStudent(student);
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

    @GetMapping("/students/filtered")
    private List<Student> getStudentsFiltered() {
        log.info("fetching students filtered");
        return Collections.emptyList();
    }

    @PostMapping(path = "/students/{id}/upload-photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Object> uploadPhoto(
            @PathVariable("id") BigInteger studentId,
            @RequestParam("file") MultipartFile file) {
        log.info("uploading photo...");
        return ResponseEntity.ok().body(studentService.uploadPhoto(file, studentId));
    }

    @GetMapping(path = "/students/file/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<org.springframework.core.io.Resource> downloadPhoto(@PathVariable("filename") String filename) {

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
