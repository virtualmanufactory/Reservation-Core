-- Schemat docelowy tabeli oddzial (MySQL) + staging + log zasilenia SAP
-- Używane przy wdrożeniu na istniejącą bazę (JPA ddl-auto=validate).

CREATE TABLE IF NOT EXISTS oddzial (
    id                              INT            NOT NULL,
    oddzial                         VARCHAR(256)   NULL,
    nazwa_miejscowosci              VARCHAR(256)   NULL,
    kod_pocztowy                    VARCHAR(256)   NULL,
    nazwa_ulicy                     VARCHAR(256)   NULL,
    wojewodztwo                     VARCHAR(256)   NULL,
    powiat                          TEXT           NULL,
    gmina                           VARCHAR(256)   NULL,
    rcs                             TINYINT        NULL,
    wspolczynnik_ciepla_spalania    DOUBLE(10,2)   NULL,
    data_wstawienia                 DATETIME       NULL,
    status_na_stronie               VARCHAR(256)   NULL,
    Telefon                         VARCHAR(15)    NULL,
    email                           VARCHAR(100)   NULL,
    Rodzaj_gazu                     VARCHAR(1000)  NULL,
    Stopien_gazyfikacji             VARCHAR(1000)  NULL,
    Punkty_wejscia                  VARCHAR(1000)  NULL,
    PRIMARY KEY (id),
    KEY idx_oddzial_oddzial (oddzial),
    KEY idx_oddzial_status (status_na_stronie)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oddzial_staging (
    id                              INT            NOT NULL AUTO_INCREMENT,
    source_id                       INT            NULL,
    oddzial                         VARCHAR(256)   NULL,
    nazwa_miejscowosci              VARCHAR(256)   NULL,
    kod_pocztowy                    VARCHAR(256)   NULL,
    nazwa_ulicy                     VARCHAR(256)   NULL,
    wojewodztwo                     VARCHAR(256)   NULL,
    powiat                          TEXT           NULL,
    gmina                           VARCHAR(256)   NULL,
    rcs                             TINYINT        NULL,
    wspolczynnik_ciepla_spalania    DOUBLE(10,2)   NULL,
    data_wstawienia                 DATETIME       NULL,
    status_na_stronie               VARCHAR(256)   NULL,
    Telefon                         VARCHAR(15)    NULL,
    email                           VARCHAR(100)   NULL,
    Rodzaj_gazu                     VARCHAR(1000)  NULL,
    Stopien_gazyfikacji             VARCHAR(1000)  NULL,
    Punkty_wejscia                  VARCHAR(1000)  NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sap_sync_run_log (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    dataset         VARCHAR(64)   NOT NULL,
    strategy        VARCHAR(32)   NOT NULL,
    status          VARCHAR(16)   NOT NULL,
    received        INT           NOT NULL,
    inserted        INT           NOT NULL,
    updated         INT           NOT NULL,
    deactivated     INT           NOT NULL,
    deleted         INT           NOT NULL,
    message         LONGTEXT      NULL,
    started_at      TIMESTAMP(6)  NOT NULL,
    finished_at     TIMESTAMP(6)  NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
