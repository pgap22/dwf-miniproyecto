package sv.edu.udb.data_collector.service.mapper;

import org.springframework.stereotype.Component;
import sv.edu.udb.data_collector.controller.response.DataTypeResponse;
import sv.edu.udb.data_collector.domain.DataType;

@Component
public class DataTypeMapper {

    public DataTypeResponse toResponse(DataType e) {
        if (e == null) return null;
        return DataTypeResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .kind(e.getKind())
                .build();
    }
}
