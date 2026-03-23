-- V1__init_schema.sql
-- Initial schema: korisnici table

CREATE TABLE IF NOT EXISTS korisnici
(
    id                BIGSERIAL    PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    ime               VARCHAR(100),
    prezime           VARCHAR(100),
    email             VARCHAR(150) UNIQUE,
    aktivan           BOOLEAN      NOT NULL DEFAULT TRUE,
    uloga             VARCHAR(20)  NOT NULL,
    id_kompanije      BIGINT       NOT NULL,
    id_poslovnice     BIGINT,
    sys_created_date  TIMESTAMP    NOT NULL DEFAULT NOW(),
    sys_created_by    BIGINT,
    sys_modified_date TIMESTAMP,
    sys_modified_by   BIGINT
);

-- Default admin user
-- Password: Admin123!  (BCrypt hash)
INSERT INTO korisnici (username, password_hash, ime, prezime, email, aktivan, uloga, id_kompanije, sys_created_date)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin',
    'Korisnik',
    'admin@maloprodaja.ba',
    TRUE,
    'ADMIN',
    1,
    NOW()
)
ON CONFLICT (username) DO NOTHING;
