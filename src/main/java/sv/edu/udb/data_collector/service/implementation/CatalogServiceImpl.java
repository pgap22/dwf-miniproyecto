package sv.edu.udb.data_collector.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;
import sv.edu.udb.data_collector.controller.request.CatalogUpdateRequest;
import sv.edu.udb.data_collector.domain.Catalog;
import sv.edu.udb.data_collector.domain.CatalogItem;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.repository.CatalogItemRepository;
import sv.edu.udb.data_collector.repository.CatalogRepository;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.CatalogService;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CatalogServiceImpl implements CatalogService {

    private final CatalogRepository catalogRepository;
    private final CatalogItemRepository itemRepository;
    private final WorkspaceRepository workspaceRepository;

    // ----- Catalog -----
    @Override
    public Catalog createCatalog(String workspaceId, String name, String description) {
        Workspace ws = null;
        if (workspaceId != null && !workspaceId.isBlank()) {
            ws = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Workspace not found"));
            if (catalogRepository.existsByNameAndWorkspaceId(name, workspaceId)) {
                throw new ResponseStatusException(CONFLICT, "Catalog name already exists in this workspace");
            }
        }

        Catalog catalog = Catalog.builder()
                .name(name)
                .description(description)
                .workspace(ws)
                .build();

        return catalogRepository.save(catalog);
    }

    @Override
    @Transactional
    public Catalog updateCatalog(String catalogId, CatalogUpdateRequest request) {
        // 1. Obtenemos la entidad existente de la base de datos.
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new EntityNotFoundException("Catalog not found with id: " + catalogId));

        // 2. Actualizamos el nombre solo si se proporcionó uno nuevo.
        if (request.getName() != null && !request.getName().isBlank()) {
            // Validamos que el nuevo nombre no cree un duplicado.
            String wsId = catalog.getWorkspace().getId();
            if (!catalog.getName().equalsIgnoreCase(request.getName()) &&
                    catalogRepository.existsByNameAndWorkspaceId(request.getName(), wsId)) {

                throw new IllegalStateException("Catalog name already exists in this workspace");
            }
            catalog.setName(request.getName());
        }

        // 3. Actualizamos la descripción solo si se proporcionó una nueva.
        if (request.getDescription() != null) {
            catalog.setDescription(request.getDescription());
        }

        // 4. Guardamos la entidad con los cambios aplicados.
        return catalogRepository.save(catalog);
    }

    @Override
    public void deleteCatalog(String catalogId) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Catalog not found"));
        catalogRepository.delete(catalog); // items se borran por cascade
    }

    @Override
    @Transactional(readOnly = true)
    public Catalog getCatalog(String catalogId) {
        return catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Catalog not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Catalog> listCatalogs(String workspaceId) {
        if (workspaceId == null || workspaceId.isBlank()) {
            return catalogRepository.findAll().stream()
                    .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                    .toList();
        }
        return catalogRepository.findAllByWorkspaceIdOrderByNameAsc(workspaceId);
    }

    // ----- Items -----
    @Override
    public CatalogItem createItem(String catalogId, String value) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Catalog not found"));

        if (itemRepository.existsByCatalogIdAndValue(catalogId, value)) {
            throw new ResponseStatusException(CONFLICT, "Item value already exists in this catalog");
        }

        CatalogItem item = CatalogItem.builder()
                .catalog(catalog)
                .value(value)
                .build();

        return itemRepository.save(item);
    }

    @Override
    public CatalogItem updateItem(String catalogId, String itemId, String value) {
        CatalogItem item = itemRepository.findByIdAndCatalog_Id(itemId, catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item not found in catalog"));

        // si cambia el code, validamos unique
        if (!item.getValue().equals(value) && itemRepository.existsByCatalogIdAndValue(catalogId, value)) {
            throw new ResponseStatusException(CONFLICT, "Another item with this value already exists");
        }

        item.setValue(value);

        return itemRepository.save(item);
    }

    @Override
    public void deleteItem(String catalogId, String itemId) {
        CatalogItem item = itemRepository.findByIdAndCatalog_Id(itemId, catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item not found in catalog"));
        itemRepository.delete(item);
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogItem getItem(String catalogId, String itemId) {
        return itemRepository.findByIdAndCatalog_Id(itemId, catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item not found in catalog"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogItem> listItems(String catalogId) {
        return itemRepository.findAllByCatalog_IdOrderByValue(catalogId);
    }
}
