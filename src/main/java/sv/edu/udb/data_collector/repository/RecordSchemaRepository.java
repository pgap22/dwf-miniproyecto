package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.data_collector.domain.RecordSchema;

import java.util.List;
import java.util.Optional;


public interface RecordSchemaRepository extends JpaRepository<RecordSchema, String> {

    Optional<RecordSchema> findByWorkspaceIdAndName(String workspaceId, String name);


    List<RecordSchema> findByWorkspaceId(String workspaceId);

}
