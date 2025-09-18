package sv.edu.udb.data_collector.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "validation_rules",
        uniqueConstraints = {
                @UniqueConstraint(name = "validation_rules_name_key", columnNames = "name")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ValidationRule {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 191)
    private String name;
}
