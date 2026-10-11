-- Jaipur 311-style service catalogue: every category has an icon code and a target
-- resolution time (service level) in hours, and routes to one department.
ALTER TABLE categories ADD COLUMN code VARCHAR(40) NULL;
ALTER TABLE categories ADD COLUMN sla_hours INT NOT NULL DEFAULT 72;

INSERT INTO departments (name) VALUES
  ('Sewerage and Drainage'),
  ('Animal Control'),
  ('Enforcement and Vigilance'),
  ('Horticulture'),
  ('Public Health');

UPDATE categories SET code = 'road', sla_hours = 168 WHERE name = 'Roads';
UPDATE categories SET code = 'streetlight', sla_hours = 48 WHERE name = 'Streetlights';
UPDATE categories SET code = 'garbage', sla_hours = 24 WHERE name = 'Garbage';
UPDATE categories SET code = 'water', sla_hours = 48 WHERE name = 'Water supply';
UPDATE categories SET code = 'drain', sla_hours = 72,
  department_id = (SELECT id FROM departments WHERE name = 'Sewerage and Drainage')
  WHERE name = 'Drainage';

INSERT INTO categories (name, department_id, code, sla_hours) VALUES
  ('Sewer overflow', (SELECT id FROM departments WHERE name = 'Sewerage and Drainage'), 'sewer', 24),
  ('Waterlogging', (SELECT id FROM departments WHERE name = 'Sewerage and Drainage'), 'waterlogging', 24),
  ('Stray animals', (SELECT id FROM departments WHERE name = 'Animal Control'), 'animal', 48),
  ('Dead animal removal', (SELECT id FROM departments WHERE name = 'Animal Control'), 'carcass', 12),
  ('Encroachment', (SELECT id FROM departments WHERE name = 'Enforcement and Vigilance'), 'encroachment', 168),
  ('Illegal hoardings', (SELECT id FROM departments WHERE name = 'Enforcement and Vigilance'), 'hoarding', 120),
  ('Parks and gardens', (SELECT id FROM departments WHERE name = 'Horticulture'), 'park', 168),
  ('Fallen trees', (SELECT id FROM departments WHERE name = 'Horticulture'), 'tree', 24),
  ('Public toilets', (SELECT id FROM departments WHERE name = 'Sanitation'), 'toilet', 48),
  ('Mosquito control', (SELECT id FROM departments WHERE name = 'Public Health'), 'mosquito', 72),
  ('Damaged footpaths', (SELECT id FROM departments WHERE name = 'Roads and Infrastructure'), 'footpath', 168);
