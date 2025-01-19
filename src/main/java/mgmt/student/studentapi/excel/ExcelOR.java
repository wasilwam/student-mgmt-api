package mgmt.student.studentapi.excel;


import jakarta.persistence.*;
import lombok.*;
import mgmt.student.studentapi.entity.BaseEntity;

import java.math.BigInteger;
import java.time.LocalDateTime;


@Entity
@Table(name = "excel_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ExcelOR extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id", nullable = false, updatable = false)
    private BigInteger fileId;
    private String fileName;
    private String filePath;
    private String status; // created, failed
    private Integer numRecords;
}