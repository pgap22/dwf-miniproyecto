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

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import sv.edu.udb.data_collector.controller.request.CreateRecordRequest;
import sv.edu.udb.data_collector.controller.request.CreateRecordValueRequest;
import sv.edu.udb.data_collector.controller.request.PatchRecordRequest;
import sv.edu.udb.data_collector.domain.*;
import sv.edu.udb.data_collector.repository.*;
import sv.edu.udb.data_collector.service.RecordService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import static sv.edu.udb.data_collector.configuration.web.SecurityUtils.*;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final RecordRepository recordRepository;
    private final RecordSchemaRepository recordSchemaRepository;
    private final RecordSchemaAttributeRepository recordSchemaAttributeRepository;
    private final CatalogItemRepository catalogItemRepository;

    // ------------------- CREATE -------------------
    @Override
    @Transactional
    public RecordEntity create(String schemaId, CreateRecordRequest request, String currentUsername) {
        RecordSchema schema = recordSchemaRepository.findById(schemaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record schema not found"));

        RecordEntity record = RecordEntity.builder()
                .schema(schema)
                .build(); // createdBy lo setea JPA Auditing

        record = recordRepository.save(record);

        if (request.getValues() != null) {
            for (CreateRecordValueRequest it : request.getValues()) {
                applyAndValidate(schemaId, record, it);
            }
        }
        return recordRepository.save(record);
    }

    // ------------------- LIST -------------------
    @Override
    @Transactional(readOnly = true)
    public Page<RecordEntity> list(
            String schemaId,
            String createdByEmail,
            OffsetDateTime createdFrom,
            OffsetDateTime createdTo,
            String stringAttrId,
            String stringContains,
            String numberAttrId,
            BigDecimal numberMin,
            BigDecimal numberMax,
            String boolAttrId,
            Boolean boolValue,
            Pageable pageable
    ) {
        Specification<RecordEntity> spec = Specification.allOf(
                specBySchema(schemaId),
                specOwnedByCurrent(), // ⬅ scope por dueño
                specCreatedFrom(createdFrom),
                specCreatedTo(createdTo),
                specStringAttrLike(stringAttrId, stringContains),
                specNumberAttrBetween(numberAttrId, numberMin, numberMax),
                specBooleanAttrEquals(boolAttrId, boolValue)
        );

        return recordRepository.findAll(spec, pageable);
    }

    // ------------------- DETAIL -------------------
    @Override
    @Transactional(readOnly = true)
    public RecordEntity getOne(String schemaId, Long recordId) {
        RecordEntity rec = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
        if (!rec.getSchema().getId().equals(schemaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found in schema");
        }

        // ownership check (404 si no es tuyo)
        if (!isAdmin()) {
            String email = currentEmailOrNull();
            if (email == null || rec.getCreatedBy() == null || !email.equals(rec.getCreatedBy().getEmail())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
            }
        }

        Hibernate.initialize(rec.getValues());
        return rec;
    }

    // ------------------- PATCH -------------------
    @Override
    @Transactional
    public RecordEntity patch(String schemaId, Long recordId, PatchRecordRequest request, String currentUsername) {
        RecordEntity record = getOne(schemaId, recordId); // getOne ya valida ownership

        if (request.getRemoveAttributeIds() != null && !request.getRemoveAttributeIds().isEmpty()) {
            Set<String> remove = new HashSet<>(request.getRemoveAttributeIds());
            record.getValues().removeIf(v -> remove.contains(v.getAttribute().getId()));
        }
        if (request.getValues() != null) {
            for (CreateRecordValueRequest it : request.getValues()) {
                applyAndValidate(schemaId, record, it);
            }
        }
        return recordRepository.save(record);
    }

    // ------------------- DELETE -------------------
    @Override
    @Transactional
    public void delete(String schemaId, Long recordId) {
        RecordEntity record = getOne(schemaId, recordId); // getOne ya valida ownership
        recordRepository.delete(record);
    }

    // ===================== VALIDACIÓN + UPSERT =====================
    private void applyAndValidate(String schemaId, RecordEntity record, CreateRecordValueRequest it) {
        RecordSchemaAttribute attribute = recordSchemaAttributeRepository.findById(it.getAttributeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute not found: " + it.getAttributeId()));

        if (!attribute.getRecordSchema().getId().equals(schemaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute does not belong to schema");
        }

        CatalogItem catalogItem = null;
        if (it.getCatalogItemId() != null && !it.getCatalogItemId().isBlank()) {
            catalogItem = catalogItemRepository.findById(it.getCatalogItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Catalog item not found: " + it.getCatalogItemId()));
        }

        int nonNulls = 0;
        if (it.getStringValue() != null) nonNulls++;
        if (it.getNumberValue() != null) nonNulls++;
        if (it.getBooleanValue() != null) nonNulls++;
        if (it.getDateValue() != null) nonNulls++;
        if (it.getCatalogItemId() != null && !it.getCatalogItemId().isBlank()) nonNulls++;
        if (nonNulls > 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only one value per attribute is allowed");

        RecordValue value = record.getValues() == null ? null :
                record.getValues().stream()
                        .filter(v -> v.getAttribute().getId().equals(attribute.getId()))
                        .findFirst().orElse(null);

        if (value == null) {
            value = RecordValue.builder().record(record).attribute(attribute).build();
            record.getValues().add(value);
        }

        value.setStringValue(it.getStringValue());
        value.setNumberValue(it.getNumberValue());
        value.setBooleanValue(it.getBooleanValue());
        value.setDateValue(it.getDateValue());
        value.setCatalogItem(catalogItem);
    }

    // ===================== SPECIFICATIONS =====================
    private Specification<RecordEntity> specBySchema(String schemaId) {
        return (root, q, cb) -> cb.equal(root.get("schema").get("id"), schemaId);
    }

    private Specification<RecordEntity> specOwnedByCurrent() {
        if (isAdmin()) return (root, q, cb) -> cb.conjunction();
        String email = currentEmailOrNull();
        return (root, q, cb) -> email == null ? cb.disjunction()
                : cb.equal(root.get("createdBy").get("email"), email);
    }

    private Specification<RecordEntity> specCreatedFrom(OffsetDateTime from) {
        return (root, q, cb) -> from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    private Specification<RecordEntity> specCreatedTo(OffsetDateTime to) {
        return (root, q, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    private Specification<RecordEntity> specStringAttrLike(String attrId, String contains) {
        return (root, q, cb) -> {
            if (attrId == null || contains == null || contains.isBlank()) return cb.conjunction();
            Join<RecordEntity, RecordValue> j = root.join("values", JoinType.LEFT);
            if (q != null) q.distinct(true);
            return cb.and(
                    cb.equal(j.get("attribute").get("id"), attrId),
                    cb.like(cb.lower(j.get("stringValue")), "%" + contains.toLowerCase() + "%")
            );
        };
    }

    private Specification<RecordEntity> specNumberAttrBetween(String attrId, BigDecimal min, BigDecimal max) {
        return (root, q, cb) -> {
            if (attrId == null || (min == null && max == null)) return cb.conjunction();
            Join<RecordEntity, RecordValue> j = root.join("values", JoinType.LEFT);
            if (q != null) q.distinct(true);
            if (min != null && max != null) {
                return cb.and(cb.equal(j.get("attribute").get("id"), attrId), cb.between(j.get("numberValue"), min, max));
            } else if (min != null) {
                return cb.and(cb.equal(j.get("attribute").get("id"), attrId), cb.greaterThanOrEqualTo(j.get("numberValue"), min));
            } else {
                return cb.and(cb.equal(j.get("attribute").get("id"), attrId), cb.lessThanOrEqualTo(j.get("numberValue"), max));
            }
        };
    }

    private Specification<RecordEntity> specBooleanAttrEquals(String attrId, Boolean value) {
        return (root, q, cb) -> {
            if (attrId == null || value == null) return cb.conjunction();
            Join<RecordEntity, RecordValue> j = root.join("values", JoinType.LEFT);
            if (q != null) q.distinct(true);
            return cb.and(
                    cb.equal(j.get("attribute").get("id"), attrId),
                    cb.equal(j.get("booleanValue"), value)
            );
        };
    }
}
