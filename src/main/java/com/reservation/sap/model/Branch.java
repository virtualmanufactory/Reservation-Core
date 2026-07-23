package com.reservation.sap.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity mapped to MySQL table {@code oddzial} (SAP target dataset).
 */
@Entity
@Table(
        name = "oddzial",
        indexes = {
                @Index(name = "idx_oddzial_oddzial", columnList = "oddzial"),
                @Index(name = "idx_oddzial_status", columnList = "status_na_stronie")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Branch {

    /** Stable id from SAP / existing MySQL row (UPSERT key). */
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "oddzial", length = 256)
    private String name;

    @Column(name = "nazwa_miejscowosci", length = 256)
    private String localityName;

    @Column(name = "kod_pocztowy", length = 256)
    private String postalCode;

    @Column(name = "nazwa_ulicy", length = 256)
    private String streetName;

    @Column(name = "wojewodztwo", length = 256)
    private String province;

    @Lob
    @Column(name = "powiat", columnDefinition = "TEXT")
    private String county;

    @Column(name = "gmina", length = 256)
    private String commune;

    /** MySQL tinyint(4) */
    @Column(name = "rcs")
    private Integer rcs;

    @Column(name = "wspolczynnik_ciepla_spalania", precision = 10, scale = 2)
    private BigDecimal combustionHeatCoefficient;

    @Column(name = "data_wstawienia")
    private LocalDateTime insertedAt;

    @Column(name = "status_na_stronie", length = 256)
    private String pageStatus;

    @Column(name = "Telefon", length = 15)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "Rodzaj_gazu", length = 1000)
    private String gasType;

    @Column(name = "Stopien_gazyfikacji", length = 1000)
    private String gasificationDegree;

    @Column(name = "Punkty_wejscia", length = 1000)
    private String entryPoints;
}
