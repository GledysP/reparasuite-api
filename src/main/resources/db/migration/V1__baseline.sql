CREATE TABLE IF NOT EXISTS public.categoria_equipo (
    activa boolean NOT NULL,
    orden_visual integer NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    codigo character varying(50) NOT NULL,
    icono character varying(50),
    nombre character varying(120) NOT NULL,
    descripcion character varying(2000)
);

CREATE TABLE IF NOT EXISTS public.categoria_equipo_falla (
    activa boolean NOT NULL,
    orden_visual integer NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    categoria_equipo_id uuid NOT NULL,
    id uuid NOT NULL,
    codigo character varying(50) NOT NULL,
    nombre character varying(150) NOT NULL,
    descripcion character varying(2000)
);

CREATE TABLE IF NOT EXISTS public.cita_ot (
    created_at timestamp(6) with time zone NOT NULL,
    fin timestamp(6) with time zone NOT NULL,
    inicio timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    ot_id uuid NOT NULL,
    estado character varying(255) NOT NULL,
    CONSTRAINT cita_ot_estado_check CHECK (((estado)::text = ANY ((ARRAY['PROGRAMADA'::character varying, 'REPROGRAMADA'::character varying, 'CANCELADA'::character varying, 'COMPLETADA'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.cliente (
    portal_activo boolean NOT NULL,
    id uuid NOT NULL,
    email character varying(255),
    nombre character varying(255) NOT NULL,
    password_hash_portal character varying(255),
    telefono character varying(255)
);

CREATE TABLE IF NOT EXISTS public.equipo (
    estado_activo boolean NOT NULL,
    fecha_compra date,
    garantia_hasta date,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    categoria_equipo_id uuid,
    cliente_id uuid NOT NULL,
    created_by uuid,
    id uuid NOT NULL,
    updated_by uuid,
    codigo_equipo character varying(50) NOT NULL,
    codigo_interno character varying(50),
    marca character varying(120),
    modelo character varying(120),
    numero_serie character varying(120),
    tipo_equipo character varying(120),
    descripcion_general character varying(4000),
    notas_tecnicas character varying(4000),
    ubicacion_habitual character varying(255)
);

CREATE TABLE IF NOT EXISTS public.foto_ot (
    visible_cliente boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    ot_id uuid NOT NULL,
    url character varying(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.historial_ot (
    fecha timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    ot_id uuid NOT NULL,
    usuario_id uuid,
    actor_nombre character varying(200) NOT NULL,
    descripcion character varying(2000) NOT NULL,
    actor_tipo character varying(255) NOT NULL,
    evento character varying(255) NOT NULL,
    CONSTRAINT historial_ot_actor_tipo_check CHECK (((actor_tipo)::text = ANY ((ARRAY['USUARIO'::character varying, 'CLIENTE'::character varying, 'SISTEMA'::character varying])::text[]))),
    CONSTRAINT historial_ot_evento_check CHECK (((evento)::text = ANY ((ARRAY['OT_CREADA'::character varying, 'CAMBIO_ESTADO'::character varying, 'NOTA_AGREGADA'::character varying, 'FOTO_SUBIDA'::character varying, 'PRESUPUESTO_GUARDADO'::character varying, 'PRESUPUESTO_ENVIADO'::character varying, 'PRESUPUESTO_ACEPTADO'::character varying, 'PRESUPUESTO_RECHAZADO'::character varying, 'PAGO_MARCADO_TRANSFERENCIA'::character varying, 'PAGO_COMPROBANTE_SUBIDO'::character varying, 'CITA_RESERVADA'::character varying, 'CITA_REPROGRAMADA'::character varying, 'MENSAJE_ENVIADO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.inventario_categoria (
    activa boolean NOT NULL,
    orden_visual integer NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    codigo character varying(50) NOT NULL,
    nombre character varying(120) NOT NULL,
    descripcion character varying(2000)
);

CREATE TABLE IF NOT EXISTS public.inventario_item (
    activo boolean NOT NULL,
    controla_stock boolean NOT NULL,
    costo_promedio numeric(12,2) NOT NULL,
    permite_stock_negativo boolean NOT NULL,
    precio_venta numeric(12,2) NOT NULL,
    stock_actual numeric(12,2) NOT NULL,
    stock_maximo numeric(12,2),
    stock_minimo numeric(12,2) NOT NULL,
    ultimo_costo numeric(12,2) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    categoria_id uuid,
    created_by uuid,
    id uuid NOT NULL,
    updated_by uuid,
    unidad_medida character varying(20) NOT NULL,
    codigo_barras character varying(100),
    sku character varying(100) NOT NULL,
    marca character varying(120),
    ubicacion_almacen character varying(120),
    modelo_compatibilidad character varying(200),
    nombre character varying(200) NOT NULL,
    descripcion character varying(4000),
    notas character varying(4000),
    CONSTRAINT inventario_item_unidad_medida_check CHECK (((unidad_medida)::text = ANY ((ARRAY['UNIDAD'::character varying, 'METRO'::character varying, 'LITRO'::character varying, 'KILOGRAMO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.inventario_movimiento (
    cantidad numeric(12,2) NOT NULL,
    costo_unitario numeric(12,2),
    stock_anterior numeric(12,2) NOT NULL,
    stock_resultante numeric(12,2) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    fecha_movimiento timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    inventario_item_id uuid NOT NULL,
    referencia_id uuid,
    usuario_id uuid,
    tipo_movimiento character varying(30) NOT NULL,
    referencia_tipo character varying(50),
    motivo character varying(200),
    observacion character varying(4000),
    CONSTRAINT inventario_movimiento_tipo_movimiento_check CHECK (((tipo_movimiento)::text = ANY ((ARRAY['ENTRADA'::character varying, 'SALIDA'::character varying, 'AJUSTE_POSITIVO'::character varying, 'AJUSTE_NEGATIVO'::character varying, 'CONSUMO_OT'::character varying, 'VENTA_DIRECTA'::character varying, 'DEVOLUCION_OT'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.mensaje_ot (
    created_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    ot_id uuid NOT NULL,
    remitente_nombre character varying(200) NOT NULL,
    contenido character varying(2000) NOT NULL,
    remitente_tipo character varying(255) NOT NULL,
    CONSTRAINT mensaje_ot_remitente_tipo_check CHECK (((remitente_tipo)::text = ANY ((ARRAY['USUARIO'::character varying, 'CLIENTE'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.nota_ot (
    visible_cliente boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    ot_id uuid NOT NULL,
    contenido character varying(2000) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.orden_trabajo (
    created_at timestamp(6) with time zone NOT NULL,
    fecha_prevista timestamp(6) with time zone,
    updated_at timestamp(6) with time zone NOT NULL,
    categoria_equipo_id uuid,
    cliente_id uuid NOT NULL,
    equipo_id uuid,
    id uuid NOT NULL,
    tecnico_id uuid,
    codigo character varying(50) NOT NULL,
    direccion character varying(1000),
    falla_detectada character varying(1000),
    falla_reportada character varying(1000),
    notas_acceso character varying(2000),
    descripcion character varying(4000) NOT NULL,
    diagnostico_tecnico character varying(4000),
    trabajo_a_realizar character varying(4000),
    equipo character varying(255),
    estado character varying(255) NOT NULL,
    prioridad character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    CONSTRAINT orden_trabajo_estado_check CHECK (((estado)::text = ANY ((ARRAY['RECIBIDA'::character varying, 'PRESUPUESTO'::character varying, 'APROBADA'::character varying, 'EN_CURSO'::character varying, 'FINALIZADA'::character varying, 'CERRADA'::character varying])::text[]))),
    CONSTRAINT orden_trabajo_prioridad_check CHECK (((prioridad)::text = ANY ((ARRAY['BAJA'::character varying, 'MEDIA'::character varying, 'ALTA'::character varying])::text[]))),
    CONSTRAINT orden_trabajo_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['TIENDA'::character varying, 'DOMICILIO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.pago_ot (
    importe numeric(12,2) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    ot_id uuid NOT NULL,
    comprobante_url character varying(500),
    estado character varying(255) NOT NULL,
    CONSTRAINT pago_ot_estado_check CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'MARCADO_TRANSFERENCIA'::character varying, 'COMPROBANTE_SUBIDO'::character varying, 'CONFIRMADO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.presupuesto_ot (
    aceptacion_check boolean NOT NULL,
    importe numeric(12,2) NOT NULL,
    aceptacion_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone NOT NULL,
    responded_at timestamp(6) with time zone,
    sent_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    ot_id uuid NOT NULL,
    detalle character varying(4000) NOT NULL,
    estado character varying(255) NOT NULL,
    CONSTRAINT presupuesto_ot_estado_check CHECK (((estado)::text = ANY ((ARRAY['BORRADOR'::character varying, 'ENVIADO'::character varying, 'ACEPTADO'::character varying, 'RECHAZADO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.taller (
    id bigint NOT NULL,
    direccion character varying(255),
    email character varying(255),
    nombre character varying(255) NOT NULL,
    prefijo_ot character varying(255) NOT NULL,
    telefono character varying(255)
);

CREATE TABLE IF NOT EXISTS public.ticket_foto (
    created_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    ticket_id uuid NOT NULL,
    url character varying(500) NOT NULL,
    nombre_original character varying(255)
);

CREATE TABLE IF NOT EXISTS public.ticket_mensaje (
    created_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    ticket_id uuid NOT NULL,
    remitente_nombre character varying(200) NOT NULL,
    contenido character varying(2000) NOT NULL,
    remitente_tipo character varying(255) NOT NULL,
    CONSTRAINT ticket_mensaje_remitente_tipo_check CHECK (((remitente_tipo)::text = ANY ((ARRAY['USUARIO'::character varying, 'CLIENTE'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.ticket_solicitud (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    cliente_id uuid NOT NULL,
    id uuid NOT NULL,
    orden_trabajo_id uuid,
    tipo_servicio_sugerido character varying(20),
    estado character varying(40) NOT NULL,
    cliente_telefono_snapshot character varying(50),
    asunto character varying(200) NOT NULL,
    cliente_email_snapshot character varying(200),
    cliente_nombre_snapshot character varying(200),
    equipo character varying(200),
    direccion character varying(500),
    descripcion character varying(4000) NOT NULL,
    descripcion_falla character varying(4000),
    observaciones character varying(4000),
    CONSTRAINT ticket_solicitud_estado_check CHECK (((estado)::text = ANY ((ARRAY['ABIERTO'::character varying, 'EN_REVISION'::character varying, 'CERRADO'::character varying])::text[])))
);

CREATE TABLE IF NOT EXISTS public.usuario (
    activo boolean NOT NULL,
    id uuid NOT NULL,
    email character varying(255) NOT NULL,
    nombre character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    rol character varying(255) NOT NULL,
    usuario character varying(255) NOT NULL,
    CONSTRAINT usuario_rol_check CHECK (((rol)::text = ANY ((ARRAY['ADMIN'::character varying, 'TECNICO'::character varying])::text[])))
);

ALTER TABLE ONLY public.categoria_equipo
    ADD CONSTRAINT categoria_equipo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.categoria_equipo
    ADD CONSTRAINT categoria_equipo_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY public.categoria_equipo_falla
    ADD CONSTRAINT categoria_equipo_falla_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.cita_ot
    ADD CONSTRAINT cita_ot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_email_key UNIQUE (email);

ALTER TABLE ONLY public.equipo
    ADD CONSTRAINT equipo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.equipo
    ADD CONSTRAINT equipo_codigo_equipo_key UNIQUE (codigo_equipo);

ALTER TABLE ONLY public.foto_ot
    ADD CONSTRAINT foto_ot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.historial_ot
    ADD CONSTRAINT historial_ot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.inventario_categoria
    ADD CONSTRAINT inventario_categoria_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.inventario_categoria
    ADD CONSTRAINT inventario_categoria_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY public.inventario_item
    ADD CONSTRAINT inventario_item_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.inventario_item
    ADD CONSTRAINT inventario_item_sku_key UNIQUE (sku);

ALTER TABLE ONLY public.inventario_movimiento
    ADD CONSTRAINT inventario_movimiento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.mensaje_ot
    ADD CONSTRAINT mensaje_ot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.nota_ot
    ADD CONSTRAINT nota_ot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.orden_trabajo
    ADD CONSTRAINT orden_trabajo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.orden_trabajo
    ADD CONSTRAINT orden_trabajo_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY public.pago_ot
    ADD CONSTRAINT pago_ot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pago_ot
    ADD CONSTRAINT pago_ot_ot_id_key UNIQUE (ot_id);

ALTER TABLE ONLY public.presupuesto_ot
    ADD CONSTRAINT presupuesto_ot_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.presupuesto_ot
    ADD CONSTRAINT presupuesto_ot_ot_id_key UNIQUE (ot_id);

ALTER TABLE ONLY public.taller
    ADD CONSTRAINT taller_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.ticket_foto
    ADD CONSTRAINT ticket_foto_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.ticket_mensaje
    ADD CONSTRAINT ticket_mensaje_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.ticket_solicitud
    ADD CONSTRAINT ticket_solicitud_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_email_key UNIQUE (email);

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_usuario_key UNIQUE (usuario);

CREATE INDEX IF NOT EXISTS idx_equipo_categoria ON public.equipo USING btree (categoria_equipo_id);
CREATE INDEX IF NOT EXISTS idx_equipo_cliente ON public.equipo USING btree (cliente_id);
CREATE INDEX IF NOT EXISTS idx_equipo_numero_serie ON public.equipo USING btree (numero_serie);
CREATE INDEX IF NOT EXISTS idx_inventario_item_categoria ON public.inventario_item USING btree (categoria_id);
CREATE INDEX IF NOT EXISTS idx_inventario_item_nombre ON public.inventario_item USING btree (nombre);
CREATE INDEX IF NOT EXISTS idx_inventario_item_sku ON public.inventario_item USING btree (sku);
CREATE INDEX IF NOT EXISTS idx_inventario_mov_fecha ON public.inventario_movimiento USING btree (fecha_movimiento);
CREATE INDEX IF NOT EXISTS idx_inventario_mov_item ON public.inventario_movimiento USING btree (inventario_item_id);
CREATE INDEX IF NOT EXISTS idx_ot_categoria_equipo ON public.orden_trabajo USING btree (categoria_equipo_id);
CREATE INDEX IF NOT EXISTS idx_ot_cliente ON public.orden_trabajo USING btree (cliente_id);
CREATE INDEX IF NOT EXISTS idx_ot_equipo ON public.orden_trabajo USING btree (equipo_id);
CREATE INDEX IF NOT EXISTS idx_ot_tecnico ON public.orden_trabajo USING btree (tecnico_id);

ALTER TABLE ONLY public.categoria_equipo_falla
    ADD CONSTRAINT fk79g1pq5d4c6imihexuf2oompe FOREIGN KEY (categoria_equipo_id) REFERENCES public.categoria_equipo(id);

ALTER TABLE ONLY public.cita_ot
    ADD CONSTRAINT fk5em9p3qn2j8bv9yei4rk4gdff FOREIGN KEY (ot_id) REFERENCES public.orden_trabajo(id);

ALTER TABLE ONLY public.equipo
    ADD CONSTRAINT fkphrvbc53k7d67yho12lgmwpjm FOREIGN KEY (categoria_equipo_id) REFERENCES public.categoria_equipo(id);

ALTER TABLE ONLY public.equipo
    ADD CONSTRAINT fka4xw2u9604yusmavnpd75bhdj FOREIGN KEY (cliente_id) REFERENCES public.cliente(id);

ALTER TABLE ONLY public.foto_ot
    ADD CONSTRAINT fk3wvhqd2b1blcllipq9ijr7rey FOREIGN KEY (ot_id) REFERENCES public.orden_trabajo(id);

ALTER TABLE ONLY public.historial_ot
    ADD CONSTRAINT fkejdnech8t99tgb4ae57hj0f1l FOREIGN KEY (ot_id) REFERENCES public.orden_trabajo(id);

ALTER TABLE ONLY public.historial_ot
    ADD CONSTRAINT fk6qfl6mksw3nglw4vltwh69ji1 FOREIGN KEY (usuario_id) REFERENCES public.usuario(id);

ALTER TABLE ONLY public.inventario_item
    ADD CONSTRAINT fkop6auf9b052d4yb5txws6d2lo FOREIGN KEY (categoria_id) REFERENCES public.inventario_categoria(id);

ALTER TABLE ONLY public.inventario_movimiento
    ADD CONSTRAINT fklk0npv2x9ihqos8xuy05rwgvd FOREIGN KEY (inventario_item_id) REFERENCES public.inventario_item(id);

ALTER TABLE ONLY public.mensaje_ot
    ADD CONSTRAINT fksx45ak586fgbti0dtrso1irmp FOREIGN KEY (ot_id) REFERENCES public.orden_trabajo(id);

ALTER TABLE ONLY public.nota_ot
    ADD CONSTRAINT fkof1dwrmel2i5fs15963sbovcf FOREIGN KEY (ot_id) REFERENCES public.orden_trabajo(id);

ALTER TABLE ONLY public.orden_trabajo
    ADD CONSTRAINT fktmeokv6t6q31n79gxxhhc00m0 FOREIGN KEY (cliente_id) REFERENCES public.cliente(id);

ALTER TABLE ONLY public.orden_trabajo
    ADD CONSTRAINT fke1as8wnjp7ykokxqgg9ilcali FOREIGN KEY (tecnico_id) REFERENCES public.usuario(id);

ALTER TABLE ONLY public.orden_trabajo
    ADD CONSTRAINT fkr7b7orsqokd4woaaw82yx90a9 FOREIGN KEY (categoria_equipo_id) REFERENCES public.categoria_equipo(id);

ALTER TABLE ONLY public.orden_trabajo
    ADD CONSTRAINT fkrkvjhijr8gjj3u4j2lvyo8ngl FOREIGN KEY (equipo_id) REFERENCES public.equipo(id);

ALTER TABLE ONLY public.pago_ot
    ADD CONSTRAINT fkrtcqd8v13k33cyx315h7jtlmg FOREIGN KEY (ot_id) REFERENCES public.orden_trabajo(id);

ALTER TABLE ONLY public.presupuesto_ot
    ADD CONSTRAINT fk8hbk42o0kro2b81uhldp9juhx FOREIGN KEY (ot_id) REFERENCES public.orden_trabajo(id);

ALTER TABLE ONLY public.ticket_foto
    ADD CONSTRAINT fk8d2o1bxv2s686cjhiaxcf0usr FOREIGN KEY (ticket_id) REFERENCES public.ticket_solicitud(id);

ALTER TABLE ONLY public.ticket_mensaje
    ADD CONSTRAINT fk5v96bk0fmaoito03xohuw5r8 FOREIGN KEY (ticket_id) REFERENCES public.ticket_solicitud(id);

ALTER TABLE ONLY public.ticket_solicitud
    ADD CONSTRAINT fktnenycuogiwf3xpx9tqdlnyev FOREIGN KEY (cliente_id) REFERENCES public.cliente(id);