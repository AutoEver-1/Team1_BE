package autoever_2st.project.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class TimeStamp {

    @CreatedDate
    @Column(name = "registed_at", updatable = false)
    private LocalDateTime registedAt;


    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 탈퇴 처리 메서드
    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    // 탈퇴 여부 확인 메서드
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}