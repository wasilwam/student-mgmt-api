package mgmt.student.studentapi.excel;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class ExcelToCsvStreamingService {

    @Value("${file.excel-base-path}")
    private String EXCEL_FILE_BASE_PATH;
    @Value("${file.csv-base-path}")
    private String CSV_FILE_BASE_PATH;
    @Resource
    private ExcelRepository excelRepository;

    public Map<String, String> readAndParseExcel(String filename) {
        String FILE_PATH = getFilePaths(filename).get(0);
        String CSV_FILE_PATH = getFilePaths(filename).get(1);

        try {
            OPCPackage pkg = OPCPackage.open(FILE_PATH);
            //OPCPackage pkg = OPCPackage.open("/var/log/applications/API/dataprocessing/7371fda5e574433794c680413933094d.xlsx");
            XSSFReader reader = new XSSFReader(pkg);

            // Get the SharedStringsTable (used for shared strings in the file)
            SharedStringsTable sst = (SharedStringsTable) reader.getSharedStringsTable();

            // Set up the SAX parser using SAXParserFactory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XMLReader parser = factory.newSAXParser().getXMLReader();

            // Set the custom SheetHandler to process the Excel data
            SheetHandler handler = new SheetHandler(sst, CSV_FILE_PATH);
            parser.setContentHandler(handler);

            // Parse the first sheet
            InputSource sheetSource = new InputSource(reader.getSheetsData().next());
            parser.parse(sheetSource);

            // Close the package
            pkg.close();
            handler.csvWriter.close();
            return Map.of("from_excel", FILE_PATH.substring(FILE_PATH.lastIndexOf("/") + 1),
                    "to_csv", CSV_FILE_PATH.substring(CSV_FILE_PATH.lastIndexOf("/") + 1),
                    "error", "");
        } catch (Exception e) {
            return Map.of("from_excel", FILE_PATH.substring(FILE_PATH.lastIndexOf("/") + 1),
                    "to_csv", CSV_FILE_PATH.substring(CSV_FILE_PATH.lastIndexOf("/") + 1),
                    "error", e.getMessage());
        }
    }

    // excel file 0, csv file 1
    public List<String> getFilePaths(String filename) {
        String FILE_PATH;
        String CSV_FILE_PATH;
        if (filename == null || filename.isEmpty()) {
            filename = excelRepository.findLast().getFileName();
        }
        FILE_PATH = EXCEL_FILE_BASE_PATH + filename;
        CSV_FILE_PATH = CSV_FILE_BASE_PATH + filename + ".csv";
        try {
            if (!Files.exists(Path.of(CSV_FILE_BASE_PATH))) {
                Files.createDirectories(Path.of(CSV_FILE_BASE_PATH));
            }
        } catch (IOException e) {
            log.error("could not create CSV folders {}", e.getMessage());
        }
        return List.of(FILE_PATH, CSV_FILE_PATH);
    }
}

class SheetHandler extends org.xml.sax.helpers.DefaultHandler {
    private SharedStringsTable sst; // Use SharedStringsTable here
    private String lastContents;
    private boolean nextIsString;
    private boolean nextIsInlineString;
    private List<String> currentRow = new ArrayList<>(); // Accumulate cell values for the current row
    private List<String> headers = new ArrayList<>(); // Store column headers
    private boolean isHeaderRow = true; // Flag to identify the header row
    BufferedWriter csvWriter;
    String csvPath;

    public SheetHandler(SharedStringsTable sst, String csvPath) throws IOException { // Constructor expects SharedStringsTable
        this.sst = sst;
        this.csvPath = csvPath;
        this.csvWriter = Files.newBufferedWriter(Paths.get(csvPath));
    }

    @Override
    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) {
        if (qName.equals("c")) {
            // Check if the cell contains a shared string or inline string
            String cellType = attributes.getValue("t");
            if (cellType != null) {
                if (cellType.equals("s")) {
                    nextIsString = true; // Shared string
                } else if (cellType.equals("inlineStr")) {
                    nextIsInlineString = true; // Inline string
                }
            }
            // Debug: Print cell attributes
            //System.out.println("Cell attributes: " + attributes);
        }
        lastContents = "";
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("v") || qName.equals("is")) {
            // Handle cell value
            if (nextIsString) {
                // Convert the shared string index to the actual string
                int idx = Integer.parseInt(lastContents);
                lastContents = sst.getItemAt(idx).getString();
                nextIsString = false;
            } else if (nextIsInlineString) {
                // Inline strings are already in lastContents
                nextIsInlineString = false;
            }
            // Add the cell value to the current row
            currentRow.add(lastContents);
        }
        if (qName.equals("row")) {
            if (isHeaderRow) {
                // First row is the header row
                headers.addAll(currentRow);
                isHeaderRow = false;
                String csvHeader = String.join(",", headers) + "\n";
                try {
                    csvWriter.write(csvHeader);
                } catch (IOException e) {
                    throw new RuntimeException("error writing csvHeader: " + e);
                }
            } else {
                // Construct a map for the current row
                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i);
                    String value = i < currentRow.size() ? currentRow.get(i) : ""; // Handle missing cells
                    rowMap.put(header, value);
                }
                System.out.println(rowMap);
                StringBuilder sb = new StringBuilder();
                sb.append(rowMap.get("studentId").toString().replace(".0", "")).append(",");
                sb.append(rowMap.get("firstName").toString()).append(",");
                sb.append(rowMap.get("lastName").toString()).append(",");
                sb.append(rowMap.get("DOB").toString()).append(",");
                sb.append(rowMap.get("class").toString()).append(",");
                sb.append(rowMap.get("score").toString()).append(",");
                sb.append(rowMap.get("status").toString()).append(",");
                sb.append(rowMap.get("photoPath").toString()).append("\n");
                try {
                    csvWriter.write(sb.toString());
                } catch (IOException e) {
                    throw new RuntimeException("error writing csv row " + sb + " " + e);
                }
            }
            currentRow.clear();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        // Accumulate the cell content
        lastContents += new String(ch, start, length);
    }
}