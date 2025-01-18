package mgmt.student.studentapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity {

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;
    @Column(name = "updated_by")
    private String updatedBy;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdBy = "SYSTEM";
    }
}
