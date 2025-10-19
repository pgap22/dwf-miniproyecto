package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.data_collector.domain.CatalogItem;

import java.util.List;
import java.util.Optional;

public interface CatalogItemRepository extends JpaRepository<CatalogItem, String> {
    List<CatalogItem> findAllByCatalog_IdOrderByValue(String catalogId);

    Optional<CatalogItem> findByIdAndCatalog_Id(String id, String catalogId);

    boolean existsByCatalogIdAndValue(String catalogId, String value);

}
