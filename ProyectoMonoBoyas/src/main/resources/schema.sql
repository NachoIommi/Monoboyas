-- ── PLANTAS ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS plantas (
    id      SERIAL PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL,
    geoLat  DOUBLE PRECISION,
    geoLng  DOUBLE PRECISION
);
ALTER TABLE plantas ADD COLUMN IF NOT EXISTS geoLat DOUBLE PRECISION;
ALTER TABLE plantas ADD COLUMN IF NOT EXISTS geoLng DOUBLE PRECISION;

-- ── MONOBOYAS ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS monoboyas (
    id                   SERIAL PRIMARY KEY,
    planta_id            INT REFERENCES plantas(id),
    -- FK a operaciones se agrega con ALTER TABLE más abajo (dependencia circular)
    operacion_activa_id  INT,
    estado               VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE'
);
ALTER TABLE monoboyas ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE';

-- ── BUQUES ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS buques (
    nro_imo     INT PRIMARY KEY,
    capacidad   INT NOT NULL,
    nombre      VARCHAR(100) NOT NULL
);

-- ── USUARIOS (todas las subclases en una tabla con discriminador) ───
CREATE TABLE IF NOT EXISTS usuarios (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    contrasena  VARCHAR(100) NOT NULL,
    dni         INT UNIQUE NOT NULL,
    rol         VARCHAR(30) NOT NULL -- ADMIN, OPERADOR_LANCHA, OPERADOR_BUQUE, OPERADOR_PLANTA
);

-- ── OPERACIONES ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS operaciones (
    id                      SERIAL PRIMARY KEY,
    monoboya_id             INT REFERENCES monoboyas(id),
    buque_nro_imo           INT REFERENCES buques(nro_imo),
    operador_lancha_id      INT REFERENCES usuarios(id),
    operador_buque_id       INT REFERENCES usuarios(id),
    operador_planta_id      INT REFERENCES usuarios(id),
    estado                  VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADA',
    tipo                    VARCHAR(30),
    planta_id               INT REFERENCES plantas(id)
);
-- Columnas agregadas en iteraciones posteriores (idempotente)
ALTER TABLE operaciones ADD COLUMN IF NOT EXISTS estado   VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADA';
ALTER TABLE operaciones ADD COLUMN IF NOT EXISTS tipo     VARCHAR(30);
ALTER TABLE operaciones ADD COLUMN IF NOT EXISTS planta_id INT REFERENCES plantas(id);

-- FK circular monoboyas ↔ operaciones
-- Falla silenciosamente si ya existe (spring.sql.init.continue-on-error=true)
ALTER TABLE monoboyas
    ADD CONSTRAINT fk_monoboyas_operacion_activa
    FOREIGN KEY (operacion_activa_id) REFERENCES operaciones(id);

-- ── SENSORES ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sensores (
    id          SERIAL PRIMARY KEY,
    tipo        VARCHAR(50) NOT NULL,
    unidad      VARCHAR(20) NOT NULL,
    activo      BOOLEAN DEFAULT TRUE,
    monoboya_id INT REFERENCES monoboyas(id)
);

-- ── MEDICIONES (tabla de alto volumen) ──────────────────────────────
CREATE TABLE IF NOT EXISTS mediciones (
    id          SERIAL PRIMARY KEY,
    sensor_id   INT NOT NULL REFERENCES sensores(id),
    valor       DOUBLE PRECISION NOT NULL,
    unidad      VARCHAR(20) NOT NULL,
    timestamp   TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE mediciones ADD COLUMN IF NOT EXISTS operacion_id INT REFERENCES operaciones(id);
DELETE FROM mediciones WHERE operacion_id IS NULL;
ALTER TABLE mediciones ALTER COLUMN operacion_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_mediciones_operacion ON mediciones (operacion_id);

CREATE INDEX IF NOT EXISTS idx_mediciones_sensor_time
    ON mediciones (sensor_id, timestamp DESC);

-- ── ALERTAS ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS alertas (
    id              SERIAL PRIMARY KEY,
    tipo_alerta     VARCHAR(30) NOT NULL,
    mensaje         TEXT NOT NULL,
    id_operacion    INT REFERENCES operaciones(id),
    medicion_id     INT NOT NULL REFERENCES mediciones(id),
    timestamp       TIMESTAMP(0) NOT NULL DEFAULT NOW()
);

-- ── USUARIO_ALERTA (relación con atributos propios) ──────────────────
CREATE TABLE IF NOT EXISTS usuario_alerta (
    alerta_id            INT NOT NULL REFERENCES alertas(id),
    usuario_id           INT NOT NULL REFERENCES usuarios(id),
    fecha_recepcion      TIMESTAMP NOT NULL DEFAULT NOW(),
    reconocida           BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_reconocimiento TIMESTAMP,
    PRIMARY KEY (alerta_id, usuario_id)
);
-- Limpieza de columnas eliminadas del modelo
ALTER TABLE operaciones DROP COLUMN IF EXISTS pasaje_monoboya_barco;
ALTER TABLE operaciones DROP COLUMN IF EXISTS esta_activa;
