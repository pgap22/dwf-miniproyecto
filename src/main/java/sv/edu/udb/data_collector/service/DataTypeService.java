package sv.edu.udb.data_collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sv.edu.udb.data_collector.domain.DataType;
import sv.edu.udb.data_collector.repository.DataTypeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DataTypeService {

    private final DataTypeRepository repository;

    /**
     * Primitivos = todos excepto 'CATALOG'
     */
    public List<DataType> listPrimitives() {
        return repository.findAllByNameNotOrderByNameAsc("CATALOG");
    }

    /**
     * (Opcional) todos, por si luego lo necesitas
     */
    public List<DataType> listAll() {
        return repository.findAllByOrderByNameAsc();
    }

    public DataType getById(String id) {
        return repository.findById(id).orElse(null);
    }
}
