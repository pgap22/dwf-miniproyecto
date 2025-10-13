package sv.edu.udb.data_collector.domain;

import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "record_values")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RecordValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Record propietario
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id", nullable = false)
    private RecordEntity record;

    // Atributo (del schema) al que corresponde el valor
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private RecordSchemaAttribute attribute;

    // Campos "por tipo"
    @Column(name = "string_value")
    private String stringValue;

    @Column(name = "number_value", precision = 19, scale = 4)
    private BigDecimal numberValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "date_value")
    private OffsetDateTime dateValue;

    // Para atributos ligados a catálogo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_item_id")
    private CatalogItem catalogItem;
}
