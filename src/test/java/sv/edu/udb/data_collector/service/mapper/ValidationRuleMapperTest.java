package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sv.edu.udb.data_collector.controller.response.ValidationRuleResponse;
import sv.edu.udb.data_collector.domain.ValidationRule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationRuleMapperTest {

    // Obtenemos una instancia del mapper que MapStruct generó
    private final ValidationRuleMapper mapper = Mappers.getMapper(ValidationRuleMapper.class);

    @Test
    @DisplayName("Debe mapear una entidad ValidationRule a un ValidationRuleResponse correctamente")
    void shouldMapValidationRuleToResponse() {
        // Arrange (Organizar)
        // Creamos una entidad de prueba reflejando el nuevo diseño
        ValidationRule entity = ValidationRule.builder()
                .name("Required")
                .build();

        // Act (Actuar)
        ValidationRuleResponse response = mapper.toResponse(entity);

        // Assert (Afirmar)
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(entity.getId());
        assertThat(response.getName()).isEqualTo("Required");
    }

    @Test
    @DisplayName("Debe devolver nulo si la entidad de entrada es nula")
    void shouldReturnNullWhenEntityIsNull() {
        // Arrange
        ValidationRule entity = null;
        
        // Act
        ValidationRuleResponse response = mapper.toResponse(entity);
        
        // Assert
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Debe mapear una lista de entidades a una lista de respuestas")
    void shouldMapEntityListToResponseList() {
        // Arrange
        ValidationRule entity1 = ValidationRule.builder().name("Required").build();
        ValidationRule entity2 = ValidationRule.builder().name("Max Length").build();
        List<ValidationRule> entityList = List.of(entity1, entity2);
        
        // Act
        List<ValidationRuleResponse> responseList = mapper.toResponseList(entityList);
        
        // Assert
        assertThat(responseList).isNotNull();
        assertThat(responseList).hasSize(2);
        assertThat(responseList.get(0).getId()).isEqualTo(entity1.getId());
        assertThat(responseList.get(1).getName()).isEqualTo("Max Length");
    }
}