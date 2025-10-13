package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.data_collector.domain.RecordValue;

public interface RecordValueRepository extends JpaRepository<RecordValue, Long> {
}
