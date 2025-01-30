package mgmt.student.studentapi.excel;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
public class ExcelFileController {

    @Value("${file.max-num-generate}")
    private int maxNumGenerate;

    @Resource
    private ExcelGenerationServiceV1 excelGenerationServiceV1;
    @Resource
    private ExcelGenerationServiceV2 excelGenerationServiceV2;
    @Resource
    private ExcelToCsvService excelToCsvService;
    @Resource
    private ExcelToCsvStreamingService excelToCsvStreamingService;
    @Resource
    private CsvUploadService csvUploadService;

    @GetMapping("/file/generate-excel")
    public Map<String, String> generateExcel(@RequestParam int recordCount) {
        if (recordCount > maxNumGenerate) {
            recordCount = maxNumGenerate;
        }
        try {
            log.info("Generating excel file");
//            String generateExcel = excelGenerationServiceVT.generateExcel(recordCount);
            String generateExcel = excelGenerationServiceV2.generateExcel(recordCount);
            var filename = generateExcel.substring(generateExcel.lastIndexOf("/") + 1);
            return Map.of("filename", filename, "totalRecords", String.valueOf(recordCount));
        } catch (IOException e) {
            throw new RuntimeException("could not generate excel file" + e.getMessage());
        }
    }

    @GetMapping("/file/convert")
    public ResponseEntity<Map<String,String>> convertExcelToCsv(@RequestParam(required = false) String fileName) {
//        return ResponseEntity.ok(excelToCsvService.convertExcelToCsvWithScoreAdjustment(fileName));
        return ResponseEntity.ok(excelToCsvStreamingService.readAndParseExcel(fileName));
    }

    @PostMapping("/file/upload")
    public ResponseEntity<String> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        log.info("Received file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            csvUploadService.processCsv(file);
            return ResponseEntity.ok("File processed successfully.");
        } catch (IOException e) {
            log.error("Error processing file", e);
            return ResponseEntity.status(500).body("Error processing file: " + e.getMessage());
        }
    }
}
