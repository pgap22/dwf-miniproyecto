package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.udb.data_collector.domain.DataType;
import sv.edu.udb.data_collector.repository.DataTypeRepository;
import sv.edu.udb.data_collector.service.implementation.DataTypeServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DataTypeServiceImplTest {

    @Mock
    private DataTypeRepository repository;

    @InjectMocks
    private DataTypeServiceImpl dataTypeService;

    private DataType stringType;
    private DataType numberType;
    private DataType catalogType;

    @BeforeEach
    void setUp() {
        // Arrange (preparación común)
        stringType = DataType.builder().id("dt-1").name("STRING").build();
        numberType = DataType.builder().id("dt-2").name("NUMBER").build();
        catalogType = DataType.builder().id("dt-3").name("CATALOG").build();
    }

    @Test
    @DisplayName("Debe listar todos los tipos de dato primitivos (excluyendo CATALOG)")
    void listPrimitives_shouldReturnPrimitiveDataTypes() {
        // Arrange
        List<DataType> primitives = List.of(stringType, numberType);
        given(repository.findAllByNameNotOrderByNameAsc("CATALOG")).willReturn(primitives);

        // Act
        List<DataType> result = dataTypeService.listPrimitives();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).doesNotContain(catalogType);
        verify(repository).findAllByNameNotOrderByNameAsc("CATALOG");
    }

    @Test
    @DisplayName("Debe listar todos los tipos de dato")
    void listAll_shouldReturnAllDataTypes() {
        // Arrange
        List<DataType> allTypes = List.of(catalogType, numberType, stringType);
        given(repository.findAllByOrderByNameAsc()).willReturn(allTypes);

        // Act
        List<DataType> result = dataTypeService.listAll();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).contains(catalogType);
        verify(repository).findAllByOrderByNameAsc();
    }

    @Test
    @DisplayName("Debe obtener un DataType por su ID cuando existe")
    void getById_whenFound_shouldReturnDataType() {
        // Arrange
        given(repository.findById("dt-1")).willReturn(Optional.of(stringType));

        // Act
        DataType result = dataTypeService.getById("dt-1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("dt-1");
        verify(repository).findById("dt-1");
    }

    @Test
    @DisplayName("Debe devolver nulo si el DataType no se encuentra por su ID")
    void getById_whenNotFound_shouldReturnNull() {
        // Arrange
        given(repository.findById("dt-nonexistent")).willReturn(Optional.empty());

        // Act
        DataType result = dataTypeService.getById("dt-nonexistent");

        // Assert
        assertThat(result).isNull();
        verify(repository).findById("dt-nonexistent");
    }
}