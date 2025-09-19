package sv.edu.udb.data_collector.domain;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "validation_rules", uniqueConstraints = {
                @UniqueConstraint(name = "validation_rules_name_key", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationRule {
        @Id
        @UuidGenerator // Genera UUIDs automáticamente (si prefieres String “cuid”, mira la nota abajo)
        @Column(nullable = false, updatable = false, length = 36)
        private String id;

        @Column(nullable = false, unique = true, length = 191)
        private String name;
}
