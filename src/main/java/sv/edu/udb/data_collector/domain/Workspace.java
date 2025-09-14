package sv.edu.udb.data_collector.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "workspaces")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Workspace {

    @Id
    @UuidGenerator // Genera UUIDs automáticamente (si prefieres String “cuid”, mira la nota abajo)
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(nullable = false, updatable = false, name = "createdAt")
    private Instant createdAt;

    @Column(nullable = false, name = "updatedAt")
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
