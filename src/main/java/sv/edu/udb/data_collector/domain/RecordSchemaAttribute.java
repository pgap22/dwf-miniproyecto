package sv.edu.udb.data_collector.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

/**
 * Representa un atributo específico dentro de un RecordScheme.
 * Define las propiedades de un campo, como su nombre, tipo de dato y si es requerido.
 */
@Entity
@Table(name = "record_schema_attributes", uniqueConstraints = {
    // Corresponde a: UNIQUE INDEX `record_schema_attributes_recordSchemaId_name_key`
    @UniqueConstraint(columnNames = {"recordSchemaId", "name"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RecordSchemaAttribute {

    @Id
    @UuidGenerator
    @Column(length = 191)
    private String id;

    @Column(nullable = false, length = 191)
    private String name;

    /**
     * Indica si el atributo es obligatorio.
     * Mapea la columna `isRequired` TINYINT(1). El valor por defecto (false) se
     * establece a nivel de campo en Java.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean isRequired = false;

    /**
     * Indica si el atributo puede tener múltiples valores.
     * Mapea la columna `allowMultiple` TINYINT(1).
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean allowMultiple = false;
    
    /**
     * Relación con el RecordScheme al que pertenece este atributo.
     * Es una relación obligatoria.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "recordSchemaId",
        nullable = false,
        foreignKey = @ForeignKey(name = "record_schema_attributes_recordSchemaId_fkey")
    )
    private RecordSchema recordSchema;

    /**
     * Relación con el tipo de dato del atributo (ej. Texto, Número, Fecha).
     * Es una relación obligatoria.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "dataTypeId",
        nullable = false,
        foreignKey = @ForeignKey(name = "record_schema_attributes_dataTypeId_fkey")
    )
    private DataType dataType;

    /**
     * Relación opcional con un catálogo de datos.
     * Se usa cuando el tipo de dato es 'Catálogo', para vincularlo a una lista
     * predefinida de valores.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "catalogId",
        // 'nullable = true' es el valor por defecto, por lo que se puede omitir
        foreignKey = @ForeignKey(name = "record_schema_attributes_catalogId_fkey")
    )
    private Catalog catalog;
}