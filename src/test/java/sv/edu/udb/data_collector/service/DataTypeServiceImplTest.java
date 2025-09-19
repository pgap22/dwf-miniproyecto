package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.response.DataTypeResponse;
import sv.edu.udb.data_collector.domain.DataType;
import sv.edu.udb.data_collector.repository.DataTypeRepository;
import sv.edu.udb.data_collector.service.implementation.DataTypeServiceImpl;
import sv.edu.udb.data_collector.service.mapper.DataTypeMapper;
import sv.edu.udb.data_collector.service.mapper.DataTypeMapperImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DataTypeServiceImplTest {

    @Mock
    private DataTypeRepository repository;

    @Spy
    private DataTypeMapper dataTypeMapper = new DataTypeMapperImpl();

    @InjectMocks
    private DataTypeServiceImpl dataTypeService;

    private DataType stringType;
    private DataType numberType;
    private DataType catalogType;

    @BeforeEach
    void setUp() {
        // Arrange (preparación común)
        stringType = DataType.builder().id("dt-1").name("STRING").kind("PRIMITIVE").build();
        numberType = DataType.builder().id("dt-2").name("NUMBER").kind("PRIMITIVE").build();
        catalogType = DataType.builder().id("dt-3").name("CATALOG").kind("CATALOG").build();
    }

    @Test
    @DisplayName("Debe listar todos los tipos de dato primitivos (excluyendo CATALOG) y retornar DTOs")
    void listPrimitives_shouldReturnPrimitiveDataTypes() {
        // Arrange
        List<DataType> primitives = List.of(stringType, numberType);
        given(repository.findAllByNameNotOrderByNameAsc("CATALOG")).willReturn(primitives);

        // Act
        List<DataTypeResponse> result = dataTypeService.listPrimitives();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("STRING");
        assertThat(result.get(1).getName()).isEqualTo("NUMBER");
        verify(repository).findAllByNameNotOrderByNameAsc("CATALOG");
    }

    @Test
    @DisplayName("Debe listar todos los tipos de dato y retornar DTOs")
    void listAll_shouldReturnAllDataTypes() {
        // Arrange
        List<DataType> allTypes = List.of(stringType, numberType, catalogType);
        given(repository.findAllByOrderByNameAsc()).willReturn(allTypes);

        // Act
        List<DataTypeResponse> result = dataTypeService.listAll();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(2).getName()).isEqualTo("CATALOG");
        verify(repository).findAllByOrderByNameAsc();
    }

    @Test
    @DisplayName("Debe obtener un DataType por su ID cuando existe y retornar un DTO")
    void getById_whenFound_shouldReturnDataType() {
        // Arrange
        given(repository.findById("dt-1")).willReturn(Optional.of(stringType));

        // Act
        DataTypeResponse result = dataTypeService.getById("dt-1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("dt-1");
        assertThat(result.getName()).isEqualTo("STRING");
        verify(repository).findById("dt-1");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el DataType no se encuentra por su ID")
    void getById_whenNotFound_shouldThrowException() {
        // Arrange
        given(repository.findById("dt-nonexistent")).willReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            dataTypeService.getById("dt-nonexistent");
        });
        verify(repository).findById("dt-nonexistent");
    }
}