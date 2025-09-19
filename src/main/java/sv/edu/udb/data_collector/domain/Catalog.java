package sv.edu.udb.data_collector.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "catalogs", uniqueConstraints = {
        @UniqueConstraint(name = "catalogs_name_key", columnNames = {"workspaceId", "name"})
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Catalog {

    @Id
    @UuidGenerator // Genera UUIDs automáticamente (si prefieres String “cuid”, mira la nota abajo)
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = true,columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspaceId",
            foreignKey = @ForeignKey(name = "catalogs_workspaceId_fkey"))
    private Workspace workspace;

    @OneToMany(mappedBy = "catalog", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Builder.Default
    private List<CatalogItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
