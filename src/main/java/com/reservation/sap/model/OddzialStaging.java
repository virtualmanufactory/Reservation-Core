package com.reservation.sap.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * Tabela stagingowa — pełny snapshot z SAP ląduje tu przed promocją do {@link Oddzial}.
 * Przy błędzie transakcji dotychczasowe dane w {@code oddzial} pozostają nietknięte.
 */
@Entity
@Table(name = "oddzial_staging")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OddzialStaging {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    private Integer id;

    /** Id rekordu docelowego z SAP (null = nowy rekord). */
    @Column(name = "source_id")
    private Integer sourceId;

    @Column(name = "oddzial", length = 256)
    private String oddzial;

    @Column(name = "nazwa_miejscowosci", length = 256)
    private String nazwaMiejscowosci;

    @Column(name = "kod_pocztowy", length = 256)
    private String kodPocztowy;

    @Column(name = "nazwa_ulicy", length = 256)
    private String nazwaUlicy;

    @Column(name = "wojewodztwo", length = 256)
    private String wojewodztwo;

    @Lob
    @Column(name = "powiat", columnDefinition = "TEXT")
    private String powiat;

    @Column(name = "gmina", length = 256)
    private String gmina;

    @Column(name = "rcs")
    private Integer rcs;

    @Column(name = "wspolczynnik_ciepla_spalania", precision = 10, scale = 2)
    private BigDecimal wspolczynnikCieplaSpalania;

    @Column(name = "data_wstawienia")
    private LocalDateTime dataWstawienia;

    @Column(name = "status_na_stronie", length = 256)
    private String statusNaStronie;

    @Column(name = "Telefon", length = 15)
    private String telefon;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "Rodzaj_gazu", length = 1000)
    private String rodzajGazu;

    @Column(name = "Stopien_gazyfikacji", length = 1000)
    private String stopienGazyfikacji;

    @Column(name = "Punkty_wejscia", length = 1000)
    private String punktyWejscia;
}
