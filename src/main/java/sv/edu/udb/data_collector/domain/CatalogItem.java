package sv.edu.udb.data_collector.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "catalog_items", uniqueConstraints = {
        @UniqueConstraint(name = "uq_item_code_per_catalog", columnNames = {"catalog_id", "code"})
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CatalogItem {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_id",
            nullable = false, foreignKey = @ForeignKey(name = "fk_item_catalog"))
    private Catalog catalog;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 160)
    private String label;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

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
