CREATE TABLE IF NOT EXISTS public.refresh_token (
    id uuid PRIMARY KEY,
    subject_id uuid NOT NULL,
    subject_type varchar(30) NOT NULL,
    token_hash varchar(128) NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    revoked_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone NOT NULL,
    last_used_at timestamp(6) with time zone,
    ip_address varchar(100),
    user_agent varchar(500),
    replaced_by_token_hash varchar(128)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_refresh_token_token_hash
    ON public.refresh_token (token_hash);

CREATE INDEX IF NOT EXISTS idx_refresh_token_subject
    ON public.refresh_token (subject_id, subject_type);

CREATE INDEX IF NOT EXISTS idx_refresh_token_expires_at
    ON public.refresh_token (expires_at);

CREATE INDEX IF NOT EXISTS idx_refresh_token_revoked_at
    ON public.refresh_token (revoked_at);