package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.data_collector.domain.Catalog;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository extends JpaRepository<Catalog, String> {
    List<Catalog> findAllByWorkspaceIdOrderByNameAsc(String workspaceId);
    Optional<Catalog> findByIdAndWorkspaceId(String id, String workspaceId);
    boolean existsByNameAndWorkspaceId(String name, String workspaceId);
}
