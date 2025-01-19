package mgmt.student.studentapi.excel;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class ExcelGenerationServiceVT {

    @Resource
    private ExcelRepository excelRepository;

    private static final String[] STUDENT_CLASSES = {"Class1", "Class2", "Class3", "Class4", "Class5"};

    public String generateExcel(int recordCount) throws IOException {
        String excelFilename = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
        String filePath = "/home/mark/source/java/student-mgmt/student-api/src/main/resources/exports/" + excelFilename;
        ExcelOR excelOR = new ExcelOR();
        excelOR.setFilePath(filePath);
        excelOR.setFileName(excelFilename);
        excelOR.setStatus("PENDING");
        excelOR.setCreatedAt(LocalDateTime.now());
        excelOR.setNumRecords(recordCount);
        ExcelOR excelOR1 = excelRepository.save(excelOR);
        log.info("created ExcelOR {}, ID {}", excelOR1, excelOR1.getFileId());

        Workbook workbook = new SXSSFWorkbook(100); // Use SXSSFWorkbook for streaming data
        Sheet sheet = workbook.createSheet("Student Data");
        log.info("created workbook and sheet");

        // Create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("studentId");
        header.createCell(1).setCellValue("firstName");
        header.createCell(2).setCellValue("lastName");
        header.createCell(3).setCellValue("DOB");
        header.createCell(4).setCellValue("class");
        header.createCell(5).setCellValue("score");
        header.createCell(6).setCellValue("status");
        header.createCell(7).setCellValue("photoPath");

        int batchSize = 10000;  // Split into smaller batches
        int batchCount = (recordCount + batchSize - 1) / batchSize;

        // Create a virtual thread pool to run tasks in parallel
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        try {
            for (int i = 0; i < batchCount; i++) {
                final int batchStart = i * batchSize;
                executorService.submit(() -> {
                    // Process each batch in parallel using ForkJoinTask
                    processBatch(sheet, batchStart, batchSize, recordCount, workbook);
                });
            }

            // Shut down the executor service after tasks are complete
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                // Wait for all threads to finish
            }

            // Write to file after processing all batches
            Path exportPath = Path.of("/home/mark/source/java/student-mgmt/student-api/src/main/resources/exports");
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
            }
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
                excelOR.setStatus("Success");
                log.info("File written successfully to {}", filePath);
            } finally {
                excelOR.setStatus("Failed");
                excelRepository.save(excelOR);
            }
        } finally {
            workbook.close();
        }
        return filePath;
    }

    // Method to process a batch of records in parallel
    private void processBatch(Sheet sheet, int batchStart, int batchSize, int totalRecords, Workbook workbook) {
        Random random = new Random();
        for (int i = batchStart; i < Math.min(batchStart + batchSize, totalRecords); i++) {
            Row row = sheet.createRow(i + 1);  // offset by 1 for header
            row.createCell(0).setCellValue(i + 1);  // studentId
            row.createCell(1).setCellValue(generateRandomString(3, 8));  // firstName
            row.createCell(2).setCellValue(generateRandomString(3, 8));  // lastName
            row.createCell(3).setCellValue(generateRandomDOB());  // DOB
            row.createCell(4).setCellValue(STUDENT_CLASSES[random.nextInt(STUDENT_CLASSES.length)]);  // class
            row.createCell(5).setCellValue(55 + random.nextInt(31));  // score
            row.createCell(6).setCellValue(random.nextInt(2));  // status (0 or 1)
            row.createCell(7).setCellValue("empty");  // photoPath (empty string)
        }
    }

    private String generateRandomString(int minLength, int maxLength) {
        Random random = new Random();
        int length = random.nextInt(maxLength - minLength + 1) + minLength;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(26)));  // Random character between 'a' and 'z'
        }
        return sb.toString();
    }

    private String generateRandomDOB() {
        Random random = new Random();
        int year = 2000 + random.nextInt(11);  // Year between 2000 and 2010
        int month = random.nextInt(12) + 1;  // Month between 1 and 12
        int day = random.nextInt(28) + 1;    // Day between 1 and 28 for simplicity
        return String.format("%04d-%02d-%02d", year, month, day);
    }
}
