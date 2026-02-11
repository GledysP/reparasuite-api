-- Taller (id fijo)
insert into taller (id, nombre, telefono, email, direccion, prefijo_ot)
values (1, 'Repara Suite Demo', '600111222', 'demo@reparasuite.com', 'Murcia (demo)', 'OT')
on conflict (id) do nothing;


-- Clientes
insert into cliente (id, nombre, telefono, email)
values
  (gen_random_uuid(), 'Carlos Pérez', '611000111', 'carlos@mail.com'),
  (gen_random_uuid(), 'María López', '622000222', 'maria@mail.com'),
  (gen_random_uuid(), 'Ana Ruiz', '633000333', 'ana@mail.com');

-- OTs demo (asigna primer técnico)
insert into orden_trabajo (id, codigo, estado, tipo, prioridad, descripcion, cliente_id, tecnico_id, fecha_prevista, direccion, notas_acceso, created_at, updated_at)
select
  gen_random_uuid(),
  'OT-0001',
  'RECIBIDA',
  'TIENDA',
  'MEDIA',
  'Portátil no enciende',
  c.id,
  u.id,
  null,
  null,
  null,
  now(),
  now()
from cliente c, usuario u
where c.nombre='Carlos Pérez' and u.usuario='tec1'
limit 1;

insert into orden_trabajo (id, codigo, estado, tipo, prioridad, descripcion, cliente_id, tecnico_id, fecha_prevista, direccion, notas_acceso, created_at, updated_at)
select
  gen_random_uuid(),
  'OT-0002',
  'EN_CURSO',
  'DOMICILIO',
  'ALTA',
  'Instalación de router y revisión de cableado',
  c.id,
  u.id,
  now() + interval '2 days',
  'C/ Demo 123',
  'Llamar al llegar',
  now(),
  now()
from cliente c, usuario u
where c.nombre='María López' and u.usuario='tec1'
limit 1;
