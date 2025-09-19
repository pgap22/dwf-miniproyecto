package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import sv.edu.udb.data_collector.controller.response.DataTypeResponse;
import sv.edu.udb.data_collector.domain.DataType;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DataTypeMapper {

    DataTypeResponse toResponse(DataType entity);
    
    List<DataTypeResponse> toResponseList(List<DataType> entities);
}