package sv.edu.udb.data_collector.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sv.edu.udb.data_collector.domain.ValidationRule;

@Repository
public interface ValidationRuleRepository extends JpaRepository<ValidationRule, String> {
    Optional<ValidationRule> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
