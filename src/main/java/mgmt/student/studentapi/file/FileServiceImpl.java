package mgmt.student.studentapi.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Override
    public boolean processFile(String fileName) {
        log.info("Processing file {}", fileName);
        return false;
    }

    @Override
    public String generateFile(int numRecords) {
        log.info("Generating file with records {}", numRecords);
        return "";
    }
}
