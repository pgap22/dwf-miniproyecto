package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.data_collector.domain.RecordEntity;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {
}
