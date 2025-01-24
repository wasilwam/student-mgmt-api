package mgmt.student.studentapi;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import mgmt.student.studentapi.student.StudentOR;
import mgmt.student.studentapi.student.StudentRespository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class InitializationComponent {

    @Resource
    private StudentRespository studentRespository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // @PostConstruct
    private void init() {

        logger.info("Start");
        logger.info("generate students");

        for (int i = 0; i < 39; i++) {
            int status = i % 2 == 0 ? 1 : 0;

            StudentOR studentOR = new StudentOR(
                    "FirstN" + i,
                    "LastN" + i,
                    generateDOB(generateRandomDate(2000, 2010)),
                    generateRandomClass(),
                    generateRandomScore(),
                    "https://i.pravatar.cc/301",
                    status);
            StudentOR saved = studentRespository.save(studentOR);
            logger.info("saved student {}", saved.getStudentId());
        }
        logger.info("End");
    }

    public String generateDOB(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, y");
        return date.format(formatter);
    }

    public LocalDate generateRandomDate(int startYear, int endYear) {
        Random random = new Random();
        int year = random.nextInt(endYear - startYear + 1) + startYear;
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1; // Assuming 28 days for simplicity

        return LocalDate.of(year, month, day);
    }

    // between 51 to 59
    public String generateRandomScore() {
        Random random = new Random();
        return String.valueOf(random.nextInt(9) + 50);
    }

    public String generateRandomClass() {
        List<String> list = Arrays.asList("Class1", "Class2", "Class3", "Class4",
                "Class5");
        Random random = new Random();
        return list.get(random.nextInt(list.size() - 1));
    }
}
