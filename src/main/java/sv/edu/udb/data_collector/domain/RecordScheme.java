package sv.edu.udb.data_collector.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

/**
 * Representa el esquema o la estructura de un tipo de registro dentro de un espacio de trabajo.
 * Cada esquema define un nombre y una descripción únicos dentro de su workspace.
 */
@Entity
@Table(name = "record_schemas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"workspaceId", "name"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RecordScheme {

    /**
     * Identificador único para el esquema de registro, generado automáticamente.
     * Corresponde a la PRIMARY KEY `id`.
     */
    @Id
    @UuidGenerator
    @Column(length = 191)
    private String id;

    /**
     * Nombre descriptivo del esquema. Debe ser único dentro de un mismo workspace.
     * Corresponde a la columna `name`.
     */
    @Column(nullable = false, length = 191)
    private String name;

    /**
     * Descripción opcional para dar más detalles sobre el propósito del esquema.
     * Corresponde a la columna `description`.
     */
    @Column(length = 191)
    private String description;

    /**
     * El Workspace al que pertenece este esquema.
     * Esta es la representación de la clave foránea `workspaceId`.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "workspaceId",
        nullable = false,
        foreignKey = @ForeignKey(name = "record_schemas_workspaceId_fkey")
    )
    private Workspace workspace;
}