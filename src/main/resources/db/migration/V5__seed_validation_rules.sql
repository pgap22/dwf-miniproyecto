INSERT INTO validation_rules (id, name) VALUES
  (UUID(), 'MIN_LENGTH'),
  (UUID(), 'MAX_LENGTH'),
  (UUID(), 'REGEX'),
  (UUID(), 'EQUAL'),
  (UUID(), 'NOT_EQUAL'),
  (UUID(), 'LESS_THAN'),
  (UUID(), 'LESS_OR_EQUAL'),
  (UUID(), 'GREATER_THAN'),
  (UUID(), 'GREATER_OR_EQUAL')
ON DUPLICATE KEY UPDATE name = VALUES(name);
