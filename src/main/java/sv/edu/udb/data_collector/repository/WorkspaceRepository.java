package sv.edu.udb.data_collector.repository;

import sv.edu.udb.data_collector.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, String> {
    boolean existsByNameIgnoreCase(String name);

    // Para el scope por dueño
    List<Workspace> findAllByCreatedBy_Email(String email);
}
