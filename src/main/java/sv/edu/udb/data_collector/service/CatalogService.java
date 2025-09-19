package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.controller.request.CatalogCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogUpdateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogItemCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogItemUpdateRequest;
import sv.edu.udb.data_collector.controller.response.CatalogResponse;
import sv.edu.udb.data_collector.controller.response.CatalogItemResponse;

import java.util.List;

public interface CatalogService {

    // Catalog
    CatalogResponse createCatalog(CatalogCreateRequest request);
    CatalogResponse updateCatalog(String catalogId, CatalogUpdateRequest request);
    void deleteCatalog(String catalogId);
    CatalogResponse getCatalog(String catalogId);
    List<CatalogResponse> listCatalogs(String workspaceId);

    // Items
    CatalogItemResponse createItem(String catalogId, CatalogItemCreateRequest request);
    CatalogItemResponse updateItem(String catalogId, String itemId, CatalogItemUpdateRequest request);
    void deleteItem(String catalogId, String itemId);
    CatalogItemResponse getItem(String catalogId, String itemId);
    List<CatalogItemResponse> listItems(String catalogId);
}