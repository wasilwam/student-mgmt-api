package mgmt.student.studentapi.file;

public interface FileService {
    String generateFile(int numRecords);
    boolean processFile(String fileName);
}
