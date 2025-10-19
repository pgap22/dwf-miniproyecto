package sv.edu.udb.data_collector.domain;

import lombok.*;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;

@Entity
@Table(name = "records")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordEntity {

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false)
    private String data; // JSON en texto

    // A qué esquema pertenece el record
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recordSchemaId", nullable = false)
    private RecordSchema schema;

    // Quién lo creó (se setea por JPA Auditing)
    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;
}
