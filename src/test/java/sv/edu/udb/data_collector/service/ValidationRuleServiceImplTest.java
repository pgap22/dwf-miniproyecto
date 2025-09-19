package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.response.ValidationRuleResponse;
import sv.edu.udb.data_collector.domain.ValidationRule;
import sv.edu.udb.data_collector.repository.ValidationRuleRepository;
import sv.edu.udb.data_collector.service.implementation.ValidationRuleServiceImpl;
import sv.edu.udb.data_collector.service.mapper.ValidationRuleMapper;
import sv.edu.udb.data_collector.service.mapper.ValidationRuleMapperImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ValidationRuleServiceImplTest {

    @Mock
    private ValidationRuleRepository repository;

    @Spy
    private ValidationRuleMapper mapper = new ValidationRuleMapperImpl();

    @InjectMocks
    private ValidationRuleServiceImpl validationRuleService;

    private ValidationRule rule1;
    private ValidationRule rule2;

    @BeforeEach
    void setUp() {
        rule1 = ValidationRule.builder().id(UUID.randomUUID().toString()).name("REQUIRED").build();
        rule2 = ValidationRule.builder().id(UUID.randomUUID().toString()).name("MAX_LENGTH").build();
    }

    @Test
    @DisplayName("Debe devolver todas las reglas de validación y convertirlas a DTOs")
    void findAll_shouldReturnAllRules() {
        // Arrange
        given(repository.findAll()).willReturn(List.of(rule1, rule2));

        // Act
        List<ValidationRuleResponse> result = validationRuleService.findAll();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("REQUIRED");
        assertThat(result.get(1).getName()).isEqualTo("MAX_LENGTH");
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Debe devolver una regla de validación por su ID y convertirla a DTO")
    void findById_whenFound_shouldReturnRuleDTO() {
        // Arrange
        given(repository.findById(rule1.getId())).willReturn(Optional.of(rule1));

        // Act
        ValidationRuleResponse result = validationRuleService.findById(rule1.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(rule1.getId());
        assertThat(result.getName()).isEqualTo(rule1.getName());
        verify(repository).findById(rule1.getId());
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException si la regla no se encuentra por su ID")
    void findById_whenNotFound_shouldThrowException() {
        // Arrange
        String nonExistentId = "id-que-no-existe";
        given(repository.findById(nonExistentId)).willReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            validationRuleService.findById(nonExistentId);
        });
        
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).contains("Regla no encontrada");
        verify(repository).findById(nonExistentId);
    }
}