CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_ot_codigo_trgm
ON public.orden_trabajo USING gin (codigo gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_ot_equipo_trgm
ON public.orden_trabajo USING gin (equipo gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_ot_falla_reportada_trgm
ON public.orden_trabajo USING gin (falla_reportada gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_ot_falla_detectada_trgm
ON public.orden_trabajo USING gin (falla_detectada gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_ot_diagnostico_tecnico_trgm
ON public.orden_trabajo USING gin (diagnostico_tecnico gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_ot_trabajo_a_realizar_trgm
ON public.orden_trabajo USING gin (trabajo_a_realizar gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_equipo_codigo_equipo_trgm
ON public.equipo USING gin (codigo_equipo gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_equipo_codigo_interno_trgm
ON public.equipo USING gin (codigo_interno gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_equipo_tipo_equipo_trgm
ON public.equipo USING gin (tipo_equipo gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_equipo_marca_trgm
ON public.equipo USING gin (marca gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_equipo_modelo_trgm
ON public.equipo USING gin (modelo gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_equipo_numero_serie_trgm
ON public.equipo USING gin (numero_serie gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_equipo_descripcion_general_trgm
ON public.equipo USING gin (descripcion_general gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_equipo_notas_tecnicas_trgm
ON public.equipo USING gin (notas_tecnicas gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_cliente_nombre_trgm
ON public.cliente USING gin (nombre gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_cliente_email_trgm
ON public.cliente USING gin (email gin_trgm_ops);
