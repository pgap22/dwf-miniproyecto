package sv.edu.udb.data_collector.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.response.DataTypeResponse;
import sv.edu.udb.data_collector.domain.DataType;
import sv.edu.udb.data_collector.repository.DataTypeRepository;
import sv.edu.udb.data_collector.service.DataTypeService;
import sv.edu.udb.data_collector.service.mapper.DataTypeMapper;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DataTypeServiceImpl implements DataTypeService {

    private final DataTypeRepository repository;
    private final DataTypeMapper dataTypeMapper;

    public List<DataTypeResponse> listPrimitives() {
        return repository.findAllByNameNotOrderByNameAsc("CATALOG").stream()
                .map(dataTypeMapper::toResponse)
                .toList();
    }

    public List<DataTypeResponse> listAll() {
        return repository.findAllByOrderByNameAsc().stream()
                .map(dataTypeMapper::toResponse)
                .toList();
    }

    public DataTypeResponse getById(String id) {
        DataType dataType = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "DataType not found with id: " + id));
        return dataTypeMapper.toResponse(dataType);
    }
}