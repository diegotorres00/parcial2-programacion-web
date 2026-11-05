-- V3: productos de investigación (ponencias, artículos y software) de un semillero

CREATE TABLE productos (
    id           BIGSERIAL    PRIMARY KEY,
    semillero_id BIGINT       NOT NULL REFERENCES semilleros(id) ON DELETE CASCADE,
    autor_id     BIGINT       NOT NULL REFERENCES integrantes(id) ON DELETE RESTRICT,
    titulo       VARCHAR(200) NOT NULL,
    tipo         VARCHAR(20)  NOT NULL,
    anio         INTEGER      NOT NULL,

    -- P3: reglas del producto reforzadas en la BD
    CONSTRAINT ck_producto_titulo_no_vacio CHECK (char_length(btrim(titulo)) > 0),
    CONSTRAINT ck_producto_tipo CHECK (tipo IN ('ponencia', 'articulo', 'software')),
    CONSTRAINT ck_producto_anio CHECK (anio BETWEEN 2000 AND 2100)
);

-- P5: no se repite el título dentro del mismo semillero (sin importar mayúsculas ni espacios en los extremos).
-- Es un índice único sobre expresión porque un UNIQUE normal no deja usar lower()/btrim().
CREATE UNIQUE INDEX uq_producto_semillero_titulo
    ON productos (semillero_id, lower(btrim(titulo)));

CREATE INDEX idx_productos_autor ON productos (autor_id);
