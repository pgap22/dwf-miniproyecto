package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.data_collector.domain.RecordSchemaAttribute;

import java.util.List;
import java.util.Optional;


public interface RecordSchemaAttributeRepository extends JpaRepository<RecordSchemaAttribute, String> {

    @Override
    @EntityGraph(attributePaths = {"dataType"})
    Optional<RecordSchemaAttribute> findById(String id);

    @EntityGraph(attributePaths = {"dataType"})
    List<RecordSchemaAttribute> findByRecordSchemaId(String recordSchemaId);

    @EntityGraph(attributePaths = {"dataType"})
    Optional<RecordSchemaAttribute> findByRecordSchemaIdAndName(String recordSchemaId, String name);

    @EntityGraph(attributePaths = {"dataType"})
    List<RecordSchemaAttribute> findByCatalogId(String catalogId);
}