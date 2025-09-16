package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.data_collector.domain.DataType;

import java.util.List;

public interface DataTypeRepository extends JpaRepository<DataType, String> {
    // Para listar todos, por nombre
    List<DataType> findAllByOrderByNameAsc();

    // Para listar primitivos (= todo excepto 'CATALOG')
    List<DataType> findAllByNameNotOrderByNameAsc(String excludedName);
}
