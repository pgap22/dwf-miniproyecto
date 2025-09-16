package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.data_collector.domain.Catalog;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository extends JpaRepository<Catalog, String> {
    List<Catalog> findAllByWorkspace_IdOrderByNameAsc(String workspaceId);
    Optional<Catalog> findByIdAndWorkspace_Id(String id, String workspaceId);
    boolean existsByNameAndWorkspace_Id(String name, String workspaceId);
}
