package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.data_collector.domain.RecordSchema;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad RecordScheme que gestiona las operaciones de base de datos.
 * Extiende JpaRepository para obtener métodos CRUD estándar.
 */
@Repository
public interface RecordSchemaRepository extends JpaRepository<RecordSchema, String> {

    Optional<RecordSchema> findByWorkspaceIdAndName(String workspaceId, String name);


    List<RecordSchema> findByWorkspaceId(String workspaceId);

}
