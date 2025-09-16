INSERT INTO data_types (id, name, kind)
VALUES 
  (UUID(), 'STRING',  'STRING'),
  (UUID(), 'NUMBER',  'NUMBER'),
  (UUID(), 'BOOLEAN', 'BOOLEAN'),
  (UUID(), 'DATE',    'DATE'),
  (UUID(), 'CATALOG', 'CATALOG')
ON DUPLICATE KEY UPDATE name = VALUES(name), kind = VALUES(kind);
