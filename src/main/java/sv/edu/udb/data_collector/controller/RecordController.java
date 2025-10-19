package sv.edu.udb.data_collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.data_collector.controller.request.CreateRecordRequest;
import sv.edu.udb.data_collector.controller.response.RecordResponse;
import sv.edu.udb.data_collector.service.RecordService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    // --- GET lista con filtros + paginación ---
    @PostMapping
    public RecordResponse create(@Valid @RequestBody CreateRecordRequest req) {
        return recordService.create(req);
    }
    
    // @GetMapping
    // public ResponseEntity<Page<RecordResponse>> list(
    //         @PathVariable String schemaId,
    //         @RequestParam(required = false) String createdByEmail,
    //         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdFrom,
    //         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdTo,
    //         @RequestParam(required = false) String stringAttrId,
    //         @RequestParam(required = false) String stringContains,
    //         @RequestParam(required = false) String numberAttrId,
    //         @RequestParam(required = false) BigDecimal numberMin,
    //         @RequestParam(required = false) BigDecimal numberMax,
    //         @RequestParam(required = false) String boolAttrId,
    //         @RequestParam(required = false) Boolean boolValue,
    //         @PageableDefault(size = 20, sort = "createdAt,desc") Pageable pageable
    // ) {
    //     Page<RecordEntity> page = recordService.list(
    //             schemaId,
    //             createdByEmail, createdFrom, createdTo,
    //             stringAttrId, stringContains,
    //             numberAttrId, numberMin, numberMax,
    //             boolAttrId, boolValue,
    //             pageable
    //     );
    //     return ResponseEntity.ok(page.map(recordMapper::toResponse));
    // }

    // // --- GET detalle ---
    // @GetMapping("/{recordId}")
    // public ResponseEntity<RecordResponse> getOne(
    //         @PathVariable String schemaId,
    //         @PathVariable Long recordId
    // ) {
    //     RecordEntity record = recordService.getOne(schemaId, recordId);
    //     return ResponseEntity.ok(recordMapper.toResponse(record));
    // }

    // // --- PATCH parcial ---
    // @PatchMapping("/{recordId}")
    // public ResponseEntity<RecordResponse> patch(
    //         @PathVariable String schemaId,
    //         @PathVariable Long recordId,
    //         @Valid @RequestBody PatchRecordRequest request,
    //         Authentication authentication
    // ) {
    //     String username = authentication != null ? authentication.getName() : null;
    //     RecordEntity updated = recordService.patch(schemaId, recordId, request, username);
    //     return ResponseEntity.ok(recordMapper.toResponse(updated));
    // }

    // // --- DELETE ---
    // @DeleteMapping("/{recordId}")
    // @ResponseStatus(HttpStatus.NO_CONTENT)
    // public void delete(@PathVariable String schemaId, @PathVariable Long recordId) {
    //     recordService.delete(schemaId, recordId);
    // }
}
