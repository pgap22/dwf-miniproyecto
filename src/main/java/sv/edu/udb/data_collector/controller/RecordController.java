package sv.edu.udb.data_collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.data_collector.controller.request.CreateRecordRequest;
import sv.edu.udb.data_collector.controller.response.RecordResponse;
import sv.edu.udb.data_collector.domain.RecordEntity;
import sv.edu.udb.data_collector.service.RecordService;
import sv.edu.udb.data_collector.service.mapper.RecordMapper;

@RestController
@RequiredArgsConstructor
@RequestMapping("/record-schemas/{schemaId}/records")
public class RecordController {

    private final RecordService recordService;
    private final RecordMapper recordMapper;

    @PostMapping
    public ResponseEntity<RecordResponse> create(
            @PathVariable String schemaId,
            @Valid @RequestBody CreateRecordRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        RecordEntity record = recordService.create(schemaId, request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(recordMapper.toResponse(record));
    }
}
