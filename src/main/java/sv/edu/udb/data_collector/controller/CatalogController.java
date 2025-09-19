package sv.edu.udb.data_collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.data_collector.controller.request.*;
import sv.edu.udb.data_collector.controller.response.CatalogItemResponse;
import sv.edu.udb.data_collector.controller.response.CatalogResponse;
import sv.edu.udb.data_collector.domain.Catalog;
import sv.edu.udb.data_collector.domain.CatalogItem;
import sv.edu.udb.data_collector.service.CatalogService;
import sv.edu.udb.data_collector.service.mapper.CatalogItemMapper;
import sv.edu.udb.data_collector.service.mapper.CatalogMapper;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/catalogs")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService service;
    private final CatalogMapper catalogMapper;
    private final CatalogItemMapper itemMapper;

    // ---- Catalog ----
    @PostMapping
    public ResponseEntity<CatalogResponse> create(@Valid @RequestBody CatalogCreateRequest req) {
        Catalog saved = service.createCatalog(req.getWorkspaceId(), req.getName(), req.getDescription());
        return ResponseEntity
                .created(URI.create("/api/catalogs/" + saved.getId()))
                .body(catalogMapper.toResponse(saved));
    }

    @PutMapping("/{catalogId}")
    public CatalogResponse update(@PathVariable String catalogId,
                                  @Valid @RequestBody CatalogUpdateRequest req) {
        return catalogMapper.toResponse(service.updateCatalog(catalogId, req));
    }

    @DeleteMapping("/{catalogId}")
    public ResponseEntity<Void> delete(@PathVariable String catalogId) {
        service.deleteCatalog(catalogId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{catalogId}")
    public CatalogResponse getOne(@PathVariable String catalogId) {
        return catalogMapper.toResponse(service.getCatalog(catalogId));
    }

    @GetMapping
    public List<CatalogResponse> list(@RequestParam(required = false) String workspaceId) {
        return service.listCatalogs(workspaceId).stream()
                .map(catalogMapper::toResponse)
                .toList();
    }

    // ---- Items ----
    @PostMapping("/{catalogId}/items")
    public ResponseEntity<CatalogItemResponse> createItem(@PathVariable String catalogId,
                                                          @Valid @RequestBody CatalogItemCreateRequest req) {
        CatalogItem saved = service.createItem(catalogId, req.getValue());
        return ResponseEntity
                .created(URI.create("/api/catalogs/" + catalogId + "/items/" + saved.getId()))
                .body(itemMapper.toResponse(saved));
    }

    @PutMapping("/{catalogId}/items/{itemId}")
    public CatalogItemResponse updateItem(@PathVariable String catalogId,
                                          @PathVariable String itemId,
                                          @Valid @RequestBody CatalogItemUpdateRequest req) {
        return itemMapper.toResponse(
                service.updateItem(catalogId, itemId, req.getValue())
        );
    }

    @DeleteMapping("/{catalogId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable String catalogId, @PathVariable String itemId) {
        service.deleteItem(catalogId, itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{catalogId}/items/{itemId}")
    public CatalogItemResponse getItem(@PathVariable String catalogId, @PathVariable String itemId) {
        return itemMapper.toResponse(service.getItem(catalogId, itemId));
    }

    @GetMapping("/{catalogId}/items")
    public List<CatalogItemResponse> listItems(@PathVariable String catalogId) {
        return service.listItems(catalogId).stream().map(itemMapper::toResponse).toList();
    }
}
