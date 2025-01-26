package mgmt.student.studentapi.excel;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;

public class LargeExcelReader {
    public static void main(String[] args) throws Exception {
        // Open the Excel file
        OPCPackage pkg = OPCPackage.open("/var/log/applications/API/dataprocessing/1589590212954930979ba01cf3284dc3.xlsx");
        XSSFReader reader = new XSSFReader(pkg);

        // Get the SharedStringsTable (used for shared strings in the file)
        SharedStrings sst = reader.getSharedStringsTable();

        // Set up the SAX parser using SAXParserFactory
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XMLReader parser = factory.newSAXParser().getXMLReader();

        // Set the custom SheetHandler to process the Excel data
        parser.setContentHandler(new SheetHandler((SharedStringsTable) sst));

        // Parse the first sheet
        InputSource sheetSource = new InputSource(reader.getSheetsData().next());
        parser.parse(sheetSource);

        // Close the package
        pkg.close();
    }
}

class SheetHandler extends org.xml.sax.helpers.DefaultHandler {
    private SharedStringsTable sst; // Use SharedStringsTable here
    private String lastContents;
    private boolean nextIsString;

    public SheetHandler(SharedStringsTable sst) { // Constructor expects SharedStringsTable
        this.sst = sst;
    }

    @Override
    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) {
        if (qName.equals("c")) {
            // Check if the cell contains a shared string
            String cellType = attributes.getValue("t");
            nextIsString = cellType != null && cellType.equals("s");
        }
        lastContents = "";
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (nextIsString) {
            // Convert the shared string index to the actual string
            int idx = Integer.parseInt(lastContents);
            lastContents = new XSSFRichTextString(sst.getItemAt(idx).getString()).toString();
            nextIsString = false;
        }
        if (qName.equals("v")) {
            // Process the cell value (e.g., print it)
            System.out.println(lastContents);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        // Accumulate the cell content
        lastContents += new String(ch, start, length);
    }
}