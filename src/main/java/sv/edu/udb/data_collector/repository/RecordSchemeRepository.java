package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.data_collector.domain.RecordScheme;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad RecordScheme que gestiona las operaciones de base de datos.
 * Extiende JpaRepository para obtener métodos CRUD estándar.
 */
@Repository
public interface RecordSchemeRepository extends JpaRepository<RecordScheme, String> {

    Optional<RecordScheme> findByWorkspaceIdAndName(String workspaceId, String name);


    List<RecordScheme> findByWorkspaceId(String workspaceId);

}
