package mgmt.student.studentapi.excel;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class ExcelToCsvService {

    @Value("${file.excel-base-path}")
    private String EXCEL_FILE_BASE_PATH;
    @Value("${file.csv-base-path}")
    private String CSV_FILE_BASE_PATH;

    @Resource
    ExcelRepository excelRepository;

    public Map<String, String> convertExcelToCsvWithScoreAdjustment(String filename) {
        String FILE_PATH;
        String CSV_FILE_PATH;

        // Determine the file path
        if (filename == null || filename.isEmpty()) {
            filename = excelRepository.findLast().getFileName();
            log.info("No filename provided, using the most recent file: {}", filename);
        }
        FILE_PATH = EXCEL_FILE_BASE_PATH + filename;
        CSV_FILE_PATH = CSV_FILE_BASE_PATH + filename + ".csv";

        log.info("Starting conversion of file: {}", FILE_PATH);

        try {
            if (!Files.exists(Path.of(CSV_FILE_BASE_PATH))) {
                Files.createDirectories(Path.of(CSV_FILE_BASE_PATH));
            }
        } catch (IOException e) {
            log.error("could not create CSV folders {}", e.getMessage());
        }

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
             FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = new XSSFWorkbook(fis);
             BufferedWriter csvWriter = Files.newBufferedWriter(Paths.get(CSV_FILE_PATH))
        ) {
            log.info("Opened Excel file successfully: {}", FILE_PATH);

            Sheet sheet = workbook.getSheetAt(0); // Read the first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                log.warn("The Excel file is empty. No data to process.");
                return null;
            }

            // Write header row
            List<String> headers = extractRowData(rowIterator.next());
            csvWriter.write(String.join(",", headers) + "\n");
            log.info("Headers written to CSV file: {}", headers);

            // Process rows
            List<Future<String>> tasks = new ArrayList<>();
            int rowCount = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowCount++;
                int currentRow = rowCount;

                // Submit row processing to virtual thread executor
                tasks.add(executorService.submit(() -> {
                    log.debug("Processing row {}", currentRow);
                    return processRow(row);
                }));
            }

            log.info("Submitted {} rows for processing.", rowCount);

            // Write processed rows
            for (Future<String> task : tasks) {
                String processedRow = task.get(); // Blocking until the task completes
                if (processedRow != null) {
                    csvWriter.write(processedRow + "\n");
                }
            }

            csvWriter.flush();
            log.info("CSV file generated successfully at: {}", CSV_FILE_PATH);

            return Map.of("from_excel", FILE_PATH.substring(FILE_PATH.lastIndexOf("/") + 1),
                    "to_csv", CSV_FILE_PATH.substring(CSV_FILE_PATH.lastIndexOf("/") + 1));
        } catch (FileNotFoundException e) {
            log.error("File not found: {}", FILE_PATH, e);
            throw new RuntimeException("Excel file not found: " + FILE_PATH, e);
        } catch (IOException e) {
            log.error("I/O error while processing the file: {}", FILE_PATH, e);
            throw new RuntimeException("I/O error while processing the Excel file: " + FILE_PATH, e);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error occurred during row processing.", e);
            throw new RuntimeException("Error while processing rows in Excel file.", e);
        }
    }

    private String processRow(Row row) {
        StringBuilder csvLine = new StringBuilder();

        for (Cell cell : row) {
            String cellValue = getCellValueAsString(cell);

            // get student score on column index 5 from excel
            if (cell.getColumnIndex() == 5) {
                try {
                    int updatedScore = Integer.parseInt(cellValue) + 10;
                    csvLine.append(updatedScore).append(",");
                    log.debug("Updated student score for cell (row {}, col {}): {}",
                            row.getRowNum(), cell.getColumnIndex(), updatedScore);
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse student score for cell (row {}, col {}): {}",
                            row.getRowNum(), cell.getColumnIndex(), cellValue);
                    csvLine.append(cellValue).append(",");
                }
            } else {
                csvLine.append(cellValue).append(",");
            }
        }

        // Remove trailing comma
        return csvLine.substring(0, csvLine.length() - 1);
    }

    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private List<String> extractRowData(Row row) {
        List<String> rowData = new ArrayList<>();
        for (Cell cell : row) {
            rowData.add(getCellValueAsString(cell));
        }
        return rowData;
    }
}
