-- Tabla para las categorías de los tickets
CREATE TABLE IF NOT EXISTS public.ticket_categoria_trabajo (
    ticket_id uuid NOT NULL REFERENCES public.ticket_solicitud(id) ON DELETE CASCADE,
    categoria varchar(50) NOT NULL
);

-- Tabla para las categorías de las OTs
CREATE TABLE IF NOT EXISTS public.ot_categoria_trabajo (
    ot_id uuid NOT NULL REFERENCES public.orden_trabajo(id) ON DELETE CASCADE,
    categoria varchar(50) NOT NULL
);

-- Migración segura: A todo lo viejo le ponemos 'REPARACION' por defecto
INSERT INTO public.ticket_categoria_trabajo (ticket_id, categoria)
SELECT id, 'REPARACION' FROM public.ticket_solicitud;

INSERT INTO public.ot_categoria_trabajo (ot_id, categoria)
SELECT id, 'REPARACION' FROM public.orden_trabajo;

-- Índices para que las búsquedas sean rápidas
CREATE INDEX IF NOT EXISTS idx_ticket_cat_trabajo ON public.ticket_categoria_trabajo(ticket_id);
CREATE INDEX IF NOT EXISTS idx_ot_cat_trabajo ON public.ot_categoria_trabajo(ot_id);