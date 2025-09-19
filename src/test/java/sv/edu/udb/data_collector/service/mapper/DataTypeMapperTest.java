package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sv.edu.udb.data_collector.controller.response.DataTypeResponse;
import sv.edu.udb.data_collector.domain.DataType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataTypeMapperTest {

    private final DataTypeMapper mapper = Mappers.getMapper(DataTypeMapper.class);

    @Test
    @DisplayName("Debe mapear una entidad DataType a un DataTypeResponse correctamente")
    void shouldMapDataTypeToDataTypeResponse() {
        // Arrange (Organizar)
        DataType entity = DataType.builder()
                .id("dt-123")
                .name("STRING")
                .kind("string")
                .build();

        // Act (Actuar)
        DataTypeResponse response = mapper.toResponse(entity);

        // Assert (Afirmar)
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("dt-123");
        assertThat(response.getName()).isEqualTo("STRING");
        assertThat(response.getKind()).isEqualTo("string");
    }

    @Test
    @DisplayName("Debe devolver nulo si la entidad de entrada es nula")
    void shouldReturnNullWhenEntityIsNull() {
        // Arrange
        DataType entity = null;
        
        // Act
        DataTypeResponse response = mapper.toResponse(entity);
        
        // Assert
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Debe mapear una lista de entidades a una lista de respuestas")
    void shouldMapEntityListToResponseList() {
        // Arrange
        DataType entity1 = DataType.builder().id("dt-1").name("STRING").build();
        DataType entity2 = DataType.builder().id("dt-2").name("NUMBER").build();
        List<DataType> entityList = List.of(entity1, entity2);
        
        // Act
        List<DataTypeResponse> responseList = mapper.toResponseList(entityList);
        
        // Assert
        assertThat(responseList).isNotNull();
        assertThat(responseList).hasSize(2);
        assertThat(responseList.get(0).getId()).isEqualTo("dt-1");
        assertThat(responseList.get(1).getName()).isEqualTo("NUMBER");
    }
}