package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestCreate;
import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestUpdate;
import sv.edu.udb.data_collector.controller.response.RecordSchemaResponse;
import sv.edu.udb.data_collector.domain.RecordSchema;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RecordSchemaMapper {

    @Mapping(target = "workspaceId", source = "workspace.id")
    RecordSchemaResponse toResponse(RecordSchema entity);

    List<RecordSchemaResponse> toResponseList(List<RecordSchema> entities);

    /**
     * Mapea un DTO de creación a una entidad RecordSchema.
     * El campo 'workspace' no se mapea, ya que se asignará en el servicio.
     */
    @Mapping(target = "workspace", ignore = true)
    RecordSchema toRecordSchema(RecordSchemaRequestCreate request);

    /**
     * Actualiza los campos de una entidad RecordSchema a partir de un DTO de actualización.
     * El campo 'workspace' no se actualiza, ya que es una relación inmutable.
     */
    @Mapping(target = "workspace", ignore = true)
    void updateRecordSchema(RecordSchemaRequestUpdate request, @MappingTarget RecordSchema entity);
}