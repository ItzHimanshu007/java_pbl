-- Jaipur 311-style service catalogue: every category has an icon code and a target
-- resolution time (service level) in hours, and routes to one department.
ALTER TABLE categories ADD COLUMN code VARCHAR(40) NULL;
ALTER TABLE categories ADD COLUMN sla_hours INT NOT NULL DEFAULT 72;

-- The existing Drainage department also takes sewer overflow and waterlogging.
UPDATE departments SET name = 'Sewerage and Drainage' WHERE name = 'Drainage';

INSERT INTO departments (name) VALUES
  ('Animal Control'),
  ('Enforcement and Vigilance'),
  ('Horticulture'),
  ('Public Health');

UPDATE categories SET code = 'road', sla_hours = 168 WHERE name = 'Roads';
UPDATE categories SET code = 'streetlight', sla_hours = 48 WHERE name = 'Streetlights';
UPDATE categories SET code = 'garbage', sla_hours = 24 WHERE name = 'Garbage';
UPDATE categories SET code = 'water', sla_hours = 48 WHERE name = 'Water supply';
UPDATE categories SET code = 'drain', sla_hours = 72 WHERE name = 'Drainage';

-- categories.id has no auto-increment (V100 assigns ids 1-5), so new ids are explicit.
INSERT INTO categories (id, name, department_id, code, sla_hours) VALUES
  (6, 'Sewer overflow', (SELECT id FROM departments WHERE name = 'Sewerage and Drainage'), 'sewer', 24),
  (7, 'Waterlogging', (SELECT id FROM departments WHERE name = 'Sewerage and Drainage'), 'waterlogging', 24),
  (8, 'Stray animals', (SELECT id FROM departments WHERE name = 'Animal Control'), 'animal', 48),
  (9, 'Dead animal removal', (SELECT id FROM departments WHERE name = 'Animal Control'), 'carcass', 12),
  (10, 'Encroachment', (SELECT id FROM departments WHERE name = 'Enforcement and Vigilance'), 'encroachment', 168),
  (11, 'Illegal hoardings', (SELECT id FROM departments WHERE name = 'Enforcement and Vigilance'), 'hoarding', 120),
  (12, 'Parks and gardens', (SELECT id FROM departments WHERE name = 'Horticulture'), 'park', 168),
  (13, 'Fallen trees', (SELECT id FROM departments WHERE name = 'Horticulture'), 'tree', 24),
  (14, 'Public toilets', (SELECT id FROM departments WHERE name = 'Sanitation'), 'toilet', 48),
  (15, 'Mosquito control', (SELECT id FROM departments WHERE name = 'Public Health'), 'mosquito', 72),
  (16, 'Damaged footpaths', (SELECT id FROM departments WHERE name = 'Roads and Infrastructure'), 'footpath', 168);
