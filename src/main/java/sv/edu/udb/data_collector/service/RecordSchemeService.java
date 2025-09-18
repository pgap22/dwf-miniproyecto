package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.domain.RecordScheme;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para gestionar la lógica de negocio de los RecordSchemes.
 * Esta versión trabaja directamente con las entidades de dominio y parámetros primitivos.
 */
public interface RecordSchemeService {

    /**
     * Crea un nuevo esquema de registro dentro de un espacio de trabajo específico.
     *
     * @param workspaceId El ID del workspace al que pertenecerá el nuevo esquema.
     * @param name El nombre para el nuevo esquema.
     * @param description La descripción opcional para el nuevo esquema.
     * @return El RecordScheme que fue guardado en la base de datos.
     */
    RecordScheme create(String workspaceId, String name, String description);

    /**
     * Busca todos los esquemas de registro que pertenecen a un workspace.
     *
     * @param workspaceId El ID del workspace.
     * @return Una lista de los esquemas encontrados; puede estar vacía.
     */
    List<RecordScheme> findAllByWorkspaceId(String workspaceId);

    /**
     * Busca un esquema de registro específico por su ID.
     *
     * @param id El ID único del esquema.
     * @return Un Optional conteniendo el RecordScheme si se encuentra, de lo contrario un Optional vacío.
     */
    Optional<RecordScheme> findById(String id);

    /**
     * Actualiza la información de un esquema de registro existente.
     *
     * @param id El ID del esquema a actualizar.
     * @param updatedData Un objeto RecordScheme que contiene los nuevos datos (ej. nombre y descripción).
     * La implementación se encargará de aplicar solo los campos permitidos.
     * @return El RecordScheme con la información actualizada.
     */
    RecordScheme update(String id, RecordScheme updatedData);

    /**
     * Elimina un esquema de registro por su ID.
     *
     * @param id El ID del esquema a eliminar.
     */
    void delete(String id);
}