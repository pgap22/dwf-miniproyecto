package sv.edu.udb.data_collector.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sv.edu.udb.data_collector.controller.request.CatalogCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogItemCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogItemUpdateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogUpdateRequest;
import sv.edu.udb.data_collector.controller.response.CatalogItemResponse;
import sv.edu.udb.data_collector.controller.response.CatalogResponse;
import sv.edu.udb.data_collector.domain.Catalog;
import sv.edu.udb.data_collector.domain.CatalogItem;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.repository.CatalogItemRepository;
import sv.edu.udb.data_collector.repository.CatalogRepository;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.CatalogService;
import sv.edu.udb.data_collector.service.mapper.CatalogItemMapper;
import sv.edu.udb.data_collector.service.mapper.CatalogMapper;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CatalogServiceImpl implements CatalogService {

    private final CatalogRepository catalogRepository;
    private final CatalogItemRepository itemRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CatalogMapper catalogMapper;
    private final CatalogItemMapper itemMapper;

    // ----- Catalog -----
    public CatalogResponse createCatalog(CatalogCreateRequest request) {
        Workspace ws = null;
        if (request.getWorkspaceId() != null && !request.getWorkspaceId().isBlank()) {
            ws = workspaceRepository.findById(request.getWorkspaceId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Workspace not found"));
            if (catalogRepository.existsByNameAndWorkspaceId(request.getName(), request.getWorkspaceId())) {
                throw new ResponseStatusException(CONFLICT, "Catalog name already exists in this workspace");
            }
        }

        Catalog catalog = catalogMapper.toCatalog(request);
        catalog.setWorkspace(ws);

        Catalog createdCatalog = catalogRepository.save(catalog);
        return catalogMapper.toResponse(createdCatalog);
    }

    
    @Transactional
    public CatalogResponse updateCatalog(String catalogId, CatalogUpdateRequest request) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Catalog not found with id: " + catalogId));

        // Validación de unicidad si el nombre cambia
        if (request.getName() != null && !request.getName().isBlank() && !catalog.getName().equalsIgnoreCase(request.getName())) {
            String wsId = catalog.getWorkspace().getId();
            if (catalogRepository.existsByNameAndWorkspaceId(request.getName(), wsId)) {
                throw new ResponseStatusException(CONFLICT, "Catalog name already exists in this workspace");
            }
        }
        
        catalogMapper.updateCatalog(request, catalog);
        Catalog updatedCatalog = catalogRepository.save(catalog);
        return catalogMapper.toResponse(updatedCatalog);
    }

    
    public void deleteCatalog(String catalogId) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Catalog not found"));
        catalogRepository.delete(catalog);
    }

    
    @Transactional(readOnly = true)
    public CatalogResponse getCatalog(String catalogId) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Catalog not found"));
        return catalogMapper.toResponse(catalog);
    }

    
    @Transactional(readOnly = true)
    public List<CatalogResponse> listCatalogs(String workspaceId) {
        return catalogRepository.findAllByWorkspaceIdOrderByNameAsc(workspaceId).stream()
                .map(catalogMapper::toResponse)
                .toList();
    }

    // ----- Items -----
    public CatalogItemResponse createItem(String catalogId, CatalogItemCreateRequest request) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Catalog not found"));

        if (itemRepository.existsByCatalogIdAndValue(catalogId, request.getValue())) {
            throw new ResponseStatusException(CONFLICT, "Item value already exists in this catalog");
        }

        CatalogItem item = itemMapper.toCatalogItem(request);
        item.setCatalog(catalog);

        CatalogItem createdItem = itemRepository.save(item);
        return itemMapper.toResponse(createdItem);
    }

    
    public CatalogItemResponse updateItem(String catalogId, String itemId, CatalogItemUpdateRequest request) {
        CatalogItem item = itemRepository.findByIdAndCatalog_Id(itemId, catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item not found in catalog"));

        if (!item.getValue().equals(request.getValue()) && itemRepository.existsByCatalogIdAndValue(catalogId, request.getValue())) {
            throw new ResponseStatusException(CONFLICT, "Another item with this value already exists");
        }
        
        itemMapper.updateCatalogItem(request, item);
        CatalogItem updatedItem = itemRepository.save(item);
        return itemMapper.toResponse(updatedItem);
    }

    
    public void deleteItem(String catalogId, String itemId) {
        CatalogItem item = itemRepository.findByIdAndCatalog_Id(itemId, catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item not found in catalog"));
        itemRepository.delete(item);
    }

    
    @Transactional(readOnly = true)
    public CatalogItemResponse getItem(String catalogId, String itemId) {
        CatalogItem item = itemRepository.findByIdAndCatalog_Id(itemId, catalogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item not found in catalog"));
        return itemMapper.toResponse(item);
    }

    
    @Transactional(readOnly = true)
    public List<CatalogItemResponse> listItems(String catalogId) {
        return itemRepository.findAllByCatalog_IdOrderByValue(catalogId).stream()
                .map(itemMapper::toResponse)
                .toList();
    }
}