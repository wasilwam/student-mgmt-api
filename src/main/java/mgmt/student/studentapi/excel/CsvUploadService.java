package mgmt.student.studentapi.excel;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import mgmt.student.studentapi.student.StudentOR;
import mgmt.student.studentapi.student.StudentRespository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class CsvUploadService {

    @Resource
    private StudentRespository studentRespository;

    public void processCsv(MultipartFile file) throws IOException {
        log.info("Processing file: {}", file.getOriginalFilename());

        // Read the file line by line
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {

            String headerLine = reader.readLine(); // Skip the header line
            log.info("CSV Header: {}", headerLine);

            String line;
            while ((line = reader.readLine()) != null) {
                String currentLine = line;
                // Submit each row for processing using a virtual thread
//                executorService.submit(() -> processEntry(currentLine));
                processEntry(currentLine);
            }
        }
    }

    private void processEntry(String entry) {
        log.info("loaded entry to database {}", entry);
        String[] fields = entry.split(",");
        log.info("split fields {}", fields.length);
        log.info("StudentId: {}, FirstName: {}, LastName: {}, DOB: {}, Class: {}, Score: {}, Status: {}, PhotoPath: {}",
                fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7]);
        try {
            var avatar = !fields[7].isEmpty() ? fields[7] : "https://i.pravatar.cc/301";
            StudentOR studentOR = new StudentOR(
                    fields[1],
                    fields[2],
                    fields[3],
                    fields[4],
                    fields[5],
                    avatar,
                    Integer.parseInt(fields[6])
            );
            StudentOR saved = studentRespository.save(studentOR);
            log.info("saved student {}", saved.getStudentId());
        } catch (Exception e) {
            log.error("error loading to db {}", e.getMessage());
        }
    }
}
