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
 * Encja odpowiadająca tabeli MySQL {@code oddzial} (schemat docelowy zasilany z SAP).
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
public class Oddzial {

    /** Id stabilne ze źródła SAP / istniejącej bazy MySQL (klucz UPSERT). */
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    private Integer id;

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

    /** tinyint(4) w MySQL */
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
