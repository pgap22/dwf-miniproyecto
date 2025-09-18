package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import sv.edu.udb.data_collector.controller.request.UpdateRecordSchemeRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemeResponse;
import sv.edu.udb.data_collector.domain.RecordScheme;

import java.util.List;

@Mapper(componentModel = "spring") // Le dice a MapStruct que genere un Spring Bean
public interface RecordSchemeMapper {

    RecordSchemeMapper INSTANCE = Mappers.getMapper(RecordSchemeMapper.class);

    /**
     * Mapea una entidad RecordScheme a un DTO de respuesta.
     * Le indicamos explícitamente cómo mapear el workspace anidado.
     * * @param entity La entidad de dominio.
     * @return El DTO de respuesta.
     */
    @Mapping(source = "workspace.id", target = "workspaceId")
    RecordSchemeResponse toResponseDTO(RecordScheme entity);
    
    /**
     * MapStruct sabe cómo mapear listas automáticamente si sabe cómo mapear el objeto individual.
     * * @param schemes Lista de entidades de dominio.
     * @return Lista de DTOs de respuesta.
     */
    List<RecordSchemeResponse> toResponseDTOList(List<RecordScheme> schemes);

    /**
     * Actualiza una entidad existente a partir de un DTO de petición.
     * '@MappingTarget' es clave: le dice a MapStruct que modifique la entidad
     * existente en lugar de crear una nueva.
     * * @param request El DTO con los datos nuevos.
     * @param entity La entidad a actualizar (obtenida de la BD).
     */
    void updateFromRequest(UpdateRecordSchemeRequest request, @MappingTarget RecordScheme entity);
}