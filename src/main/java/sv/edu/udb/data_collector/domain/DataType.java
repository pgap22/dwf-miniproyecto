package sv.edu.udb.data_collector.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "data_types")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DataType {

    @Id
    private String id; // generado por el seed (UUID()), no por JPA

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String kind;
}
