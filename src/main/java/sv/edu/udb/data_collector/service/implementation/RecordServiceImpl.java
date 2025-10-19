package sv.edu.udb.data_collector.service.implementation;

import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import sv.edu.udb.data_collector.controller.request.CreateRecordRequest;
import sv.edu.udb.data_collector.controller.request.CreateRecordValueRequest;
import sv.edu.udb.data_collector.controller.request.PatchRecordRequest;
import sv.edu.udb.data_collector.controller.response.RecordResponse;
import sv.edu.udb.data_collector.domain.*;
import sv.edu.udb.data_collector.repository.*;
import sv.edu.udb.data_collector.service.RecordService;
import sv.edu.udb.data_collector.service.mapper.RecordMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static sv.edu.udb.data_collector.configuration.web.SecurityUtils.*;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    // private final RecordRepository recordRepository;
    private final RecordMapper recordMapper;
    private final RecordSchemaRepository recordSchemaRepository;
    // private final RecordSchemaAttributeRepository
    // recordSchemaAttributeRepository;
    private final CatalogItemRepository catalogItemRepository;

    @Transactional
    public RecordResponse create(CreateRecordRequest request) {
        RecordSchema schema = recordSchemaRepository.findById(request.getSchemaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record schema not found"));

        RecordEntity record = RecordEntity.builder()
                .schema(schema)
                .build(); // userId lo setea JPA Auditing

        var data = request.getData();

        try {
            System.out.println("RAW request.getData(): " + request.getData());
            System.out.println(
                    "Parsed JSON (pretty):\n" + data.toPrettyString());
        } catch (Exception e) {
            System.out.println("ERROR al parsear JSON: " + e.getMessage());
            // seguimos igual, solo PoC
        }

        // ===== Recorrer atributos del esquema y validar por tipo (solo SOUT) =====
        java.util.List<RecordSchemaAttribute> attributes = schema.getAttribues(); // ojo: si es getAttributes(),
                                                                                  // corrígelo
        for (RecordSchemaAttribute attr : attributes) {
            System.out.println("\n====================");
            System.out.println("Atributo       : " + attr.getName());
            System.out.println(
                    "Kind/DataType  : " + (attr.getDataType() != null ? attr.getDataType().getName() : "null"));
            System.out.println("Catalog ID     : " + (attr.getCatalog() != null ? attr.getCatalog().getId() : "null"));
            System.out.println("Es requerido?  : " + attr.getIsRequired());

            // Tomamos el valor del JSON por nombre de atributo (ajusta si usas otro key, p.
            // ej. id)
            com.fasterxml.jackson.databind.JsonNode v = (data != null ? data.get(attr.getName()) : null);
            System.out.println("Valor en JSON  : " + (v == null ? "null" : v.toString()));

            // Requerido?
            if (attr.getIsRequired() != null && attr.getIsRequired() && (v == null || v.isNull())) {
                System.out.println(">> FALTA valor requerido para '" + attr.getName() + "'");
                continue; // seguimos con el siguiente
            }
            if (v == null || v.isNull()) {
                System.out.println(">> OK (no requerido, valor ausente)");
                continue;
            }

            String kind = (attr.getDataType() != null ? attr.getDataType().getName() : null);
            if (kind == null) {
                System.out.println(">> Sin DataType definido, no valido tipo.");
                continue;
            }

            switch (kind) {
                case "BOOLEAN" -> {
                    boolean ok = v.isBoolean();
                    System.out.println("Validación BOOLEAN -> " + ok + " (isBoolean=" + v.isBoolean() + ")");
                }
                case "NUMBER" -> {
                    boolean ok = v.isNumber();
                    System.out.println("Validación NUMBER  -> " + ok + " (isNumber=" + v.isNumber() + ")");
                }
                case "STRING" -> {
                    boolean ok = v.isTextual() && !v.asText().isBlank();
                    System.out.println("Validación STRING  -> " + ok + " (isTextual=" + v.isTextual() + ", blank="
                            + (v.isTextual() && v.asText().isBlank()) + ")");
                }
                case "DATE" -> {
                    // suponemos ISO-8601 (yyyy-MM-dd o fecha-hora). Probamos ambas.
                    boolean ok = false;
                    String txt = v.isTextual() ? v.asText() : null;
                    if (txt != null) {
                        try {
                            java.time.LocalDate.parse(txt); // yyyy-MM-dd
                            ok = true;
                            System.out.println("Validación DATE    -> true (LocalDate)");
                        } catch (Exception e1) {
                            try {
                                java.time.OffsetDateTime.parse(txt); // ISO date-time
                                ok = true;
                                System.out.println("Validación DATE    -> true (OffsetDateTime)");
                            } catch (Exception e2) {
                                System.out.println(
                                        "Validación DATE    -> false (no parsea LocalDate/OffsetDateTime). Valor="
                                                + txt);
                            }
                        }
                    } else {
                        System.out.println("Validación DATE    -> false (no es texto)");
                    }
                }
                case "CATALOG" -> {
                    // esperamos un STRING con el "value" del item de catálogo
                    if (!v.isTextual()) {
                        System.out.println("Validación CATALOG -> false (valor no textual)");
                    } else {
                        String val = v.asText();
                        String catalogId = (attr.getCatalog() != null ? attr.getCatalog().getId() : null);
                        if (catalogId == null) {
                            System.out.println(
                                    "Validación CATALOG -> no hay catalogId en el atributo; no valido contra repo.");
                        } else {
                            // nota: si tu repo es existsByCatalog_IdAndValue(), úsalo; si es
                            // existsByCatalogIdAndValue(), ajusta aquí
                            boolean exists = catalogItemRepository.existsByCatalogIdAndValue(catalogId, val);
                            System.out.println("Validación CATALOG -> valor='" + val + "', catalogId=" + catalogId
                                    + ", exists=" + exists);
                        }
                    }
                }
                default -> System.out.println("Kind no reconocido: " + kind + " (no se valida)");
            }
        }

        // ===== solo PoC: no persistimos, devolvemos el DTO =====
        RecordResponse responseDto = recordMapper.toResponse(record);
        System.out.println("\nListo. Devolviendo RecordResponse (PoC, sin persistir).");
        return responseDto;
    }

    // // ------------------- LIST -------------------
    // @Transactional(readOnly = true)
    // public List<RecordEntity> list(
    // String schemaId,
    // String createdByEmail,
    // OffsetDateTime createdFrom,
    // OffsetDateTime createdTo,
    // String stringAttrId,
    // String stringContains,
    // String numberAttrId,
    // BigDecimal numberMin,
    // BigDecimal numberMax,
    // String boolAttrId,
    // Boolean boolValue,
    // Pageable pageable
    // ) {
    // // Specification<RecordEntity> spec = Specification.allOf(
    // // specBySchema(schemaId),
    // // specOwnedByCurrent(), // ⬅ scope por dueño
    // // specCreatedFrom(createdFrom),
    // // specCreatedTo(createdTo),
    // // specStringAttrLike(stringAttrId, stringContains),
    // // specNumberAttrBetween(numberAttrId, numberMin, numberMax),
    // // specBooleanAttrEquals(boolAttrId, boolValue)
    // // );

    // return recordRepository.findAll();
    // }

    // // ------------------- DETAIL -------------------
    // @Transactional(readOnly = true)
    // public RecordEntity getOne(String schemaId, Long recordId) {
    // RecordEntity rec = recordRepository.findById(recordId)
    // .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record
    // not found"));
    // if (!rec.getSchema().getId().equals(schemaId)) {
    // throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found in
    // schema");
    // }

    // // ownership check (404 si no es tuyo)
    // if (!isAdmin()) {
    // String email = currentEmailOrNull();
    // if (email == null || rec.getCreatedBy() == null ||
    // !email.equals(rec.getCreatedBy().getEmail())) {
    // throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
    // }
    // }

    // Hibernate.initialize(rec.getValues());
    // return rec;
    // }

    // // ------------------- PATCH -------------------
    // @Transactional
    // public RecordEntity patch(String schemaId, Long recordId, PatchRecordRequest
    // request, String currentUsername) {
    // RecordEntity record = getOne(schemaId, recordId); // getOne ya valida
    // ownership

    // if (request.getRemoveAttributeIds() != null &&
    // !request.getRemoveAttributeIds().isEmpty()) {
    // Set<String> remove = new HashSet<>(request.getRemoveAttributeIds());
    // record.getValues().removeIf(v -> remove.contains(v.getAttribute().getId()));
    // }
    // if (request.getValues() != null) {
    // for (CreateRecordValueRequest it : request.getValues()) {
    // applyAndValidate(schemaId, record, it);
    // }
    // }
    // return recordRepository.save(record);
    // }

    // // ------------------- DELETE -------------------
    // @Transactional
    // public void delete(String schemaId, Long recordId) {
    // RecordEntity record = getOne(schemaId, recordId); // getOne ya valida
    // ownership
    // recordRepository.delete(record);
    // }

    // // ===================== VALIDACIÓN + UPSERT =====================
    // private void applyAndValidate(String schemaId, RecordEntity record,
    // CreateRecordValueRequest it) {
    // RecordSchemaAttribute attribute =
    // recordSchemaAttributeRepository.findById(it.getAttributeId())
    // .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
    // "Attribute not found: " + it.getAttributeId()));

    // if (!attribute.getRecordSchema().getId().equals(schemaId)) {
    // throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute does not
    // belong to schema");
    // }

    // CatalogItem catalogItem = null;
    // if (it.getCatalogItemId() != null && !it.getCatalogItemId().isBlank()) {
    // catalogItem = catalogItemRepository.findById(it.getCatalogItemId())
    // .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
    // "Catalog item not found: " + it.getCatalogItemId()));
    // }

    // int nonNulls = 0;
    // if (it.getStringValue() != null) nonNulls++;
    // if (it.getNumberValue() != null) nonNulls++;
    // if (it.getBooleanValue() != null) nonNulls++;
    // if (it.getDateValue() != null) nonNulls++;
    // if (it.getCatalogItemId() != null && !it.getCatalogItemId().isBlank())
    // nonNulls++;
    // if (nonNulls > 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
    // "Only one value per attribute is allowed");

    // RecordValue value = record.getValues() == null ? null :
    // record.getValues().stream()
    // .filter(v -> v.getAttribute().getId().equals(attribute.getId()))
    // .findFirst().orElse(null);

    // if (value == null) {
    // value = RecordValue.builder().record(record).attribute(attribute).build();
    // record.getValues().add(value);
    // }

    // value.setStringValue(it.getStringValue());
    // value.setNumberValue(it.getNumberValue());
    // value.setBooleanValue(it.getBooleanValue());
    // value.setDateValue(it.getDateValue());
    // value.setCatalogItem(catalogItem);
    // }

    // // ===================== SPECIFICATIONS =====================
    // private Specification<RecordEntity> specBySchema(String schemaId) {
    // return (root, q, cb) -> cb.equal(root.get("schema").get("id"), schemaId);
    // }

    // private Specification<RecordEntity> specOwnedByCurrent() {
    // if (isAdmin()) return (root, q, cb) -> cb.conjunction();
    // String email = currentEmailOrNull();
    // return (root, q, cb) -> email == null ? cb.disjunction()
    // : cb.equal(root.get("createdBy").get("email"), email);
    // }

    // private Specification<RecordEntity> specCreatedFrom(OffsetDateTime from) {
    // return (root, q, cb) -> from == null ? cb.conjunction() :
    // cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    // }

    // private Specification<RecordEntity> specCreatedTo(OffsetDateTime to) {
    // return (root, q, cb) -> to == null ? cb.conjunction() :
    // cb.lessThanOrEqualTo(root.get("createdAt"), to);
    // }

    // private Specification<RecordEntity> specStringAttrLike(String attrId, String
    // contains) {
    // return (root, q, cb) -> {
    // if (attrId == null || contains == null || contains.isBlank()) return
    // cb.conjunction();
    // Join<RecordEntity, RecordValue> j = root.join("values", JoinType.LEFT);
    // if (q != null) q.distinct(true);
    // return cb.and(
    // cb.equal(j.get("attribute").get("id"), attrId),
    // cb.like(cb.lower(j.get("stringValue")), "%" + contains.toLowerCase() + "%")
    // );
    // };
    // }

    // private Specification<RecordEntity> specNumberAttrBetween(String attrId,
    // BigDecimal min, BigDecimal max) {
    // return (root, q, cb) -> {
    // if (attrId == null || (min == null && max == null)) return cb.conjunction();
    // Join<RecordEntity, RecordValue> j = root.join("values", JoinType.LEFT);
    // if (q != null) q.distinct(true);
    // if (min != null && max != null) {
    // return cb.and(cb.equal(j.get("attribute").get("id"), attrId),
    // cb.between(j.get("numberValue"), min, max));
    // } else if (min != null) {
    // return cb.and(cb.equal(j.get("attribute").get("id"), attrId),
    // cb.greaterThanOrEqualTo(j.get("numberValue"), min));
    // } else {
    // return cb.and(cb.equal(j.get("attribute").get("id"), attrId),
    // cb.lessThanOrEqualTo(j.get("numberValue"), max));
    // }
    // };
    // }

    // private Specification<RecordEntity> specBooleanAttrEquals(String attrId,
    // Boolean value) {
    // return (root, q, cb) -> {
    // if (attrId == null || value == null) return cb.conjunction();
    // Join<RecordEntity, RecordValue> j = root.join("values", JoinType.LEFT);
    // if (q != null) q.distinct(true);
    // return cb.and(
    // cb.equal(j.get("attribute").get("id"), attrId),
    // cb.equal(j.get("booleanValue"), value)
    // );
    // };
    // }
}
