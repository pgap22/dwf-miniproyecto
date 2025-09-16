package sv.edu.udb.data_collector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.data_collector.controller.response.DataTypeResponse;
import sv.edu.udb.data_collector.service.DataTypeService;
import sv.edu.udb.data_collector.service.mapper.DataTypeMapper;

import java.util.List;

@RestController
@RequestMapping("/api/data-types")
@RequiredArgsConstructor
public class DataTypeController {

    private final DataTypeService service;
    private final DataTypeMapper mapper;

    /**
     * Requisito: Endpoints de solo lectura para "listar los tipos primitivos".
     * GET /api/data-types/primitives
     */
    @GetMapping("/primitives")
    public List<DataTypeResponse> listPrimitives() {
        return service.listPrimitives().stream()
                .map(mapper::toResponse)
                .toList();
    }

    // (Opcional) Si quisieras exponer todos:
    @GetMapping
    public List<DataTypeResponse> listAll() {
        return service.listAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    // (Opcional) detalle por id
    @GetMapping("{id}")
    public ResponseEntity<DataTypeResponse> get(@PathVariable String id) {
        var entity = service.getById(id);
        if (entity == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(mapper.toResponse(entity));
    }
}
