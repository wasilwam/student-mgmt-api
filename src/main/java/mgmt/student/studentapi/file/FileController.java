package mgmt.student.studentapi.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.LinkedHashMap;

@CrossOrigin
@Slf4j
@RestController("file")
public class FileController {

    @PostMapping(path = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<LinkedHashMap<String, String>> uploadPhoto(@RequestBody File file) {
        log.info("uploading photo");
        return ResponseEntity.ok().body(new LinkedHashMap<String, String>());
    }
}
