package sv.edu.udb.data_collector.repository;

import sv.edu.udb.data_collector.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, String> {
    boolean existsByNameIgnoreCase(String name);
}
