package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.domain.Catalog;
import sv.edu.udb.data_collector.domain.CatalogItem;

import java.util.List;

public interface CatalogService {

    // Catalog
    Catalog createCatalog(String workspaceId, String name, String description);
    Catalog updateCatalog(String catalogId, String name, String description);
    void deleteCatalog(String catalogId);
    Catalog getCatalog(String catalogId);
    List<Catalog> listCatalogs(String workspaceId); // si workspaceId es null, listar todos (según negocio)

    // Items
    CatalogItem createItem(String catalogId, String code, String label, Boolean isActive);
    CatalogItem updateItem(String catalogId, String itemId, String code, String label, Boolean isActive);
    void deleteItem(String catalogId, String itemId);
    CatalogItem getItem(String catalogId, String itemId);
    List<CatalogItem> listItems(String catalogId);
}
