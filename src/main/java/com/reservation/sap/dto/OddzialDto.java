package com.reservation.sap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Rekord snapshotu z SAP odpowiadający tabeli {@code oddzial}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OddzialDto {

    private Integer id;
    private String oddzial;

    @JsonProperty("nazwa_miejscowosci")
    private String nazwaMiejscowosci;

    @JsonProperty("kod_pocztowy")
    private String kodPocztowy;

    @JsonProperty("nazwa_ulicy")
    private String nazwaUlicy;

    private String wojewodztwo;
    private String powiat;
    private String gmina;
    private Integer rcs;

    @JsonProperty("wspolczynnik_ciepla_spalania")
    private BigDecimal wspolczynnikCieplaSpalania;

    @JsonProperty("data_wstawienia")
    private LocalDateTime dataWstawienia;

    @JsonProperty("status_na_stronie")
    private String statusNaStronie;

    @JsonProperty("Telefon")
    private String telefon;

    private String email;

    @JsonProperty("Rodzaj_gazu")
    private String rodzajGazu;

    @JsonProperty("Stopien_gazyfikacji")
    private String stopienGazyfikacji;

    @JsonProperty("Punkty_wejscia")
    private String punktyWejscia;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOddzial() {
        return oddzial;
    }

    public void setOddzial(String oddzial) {
        this.oddzial = oddzial;
    }

    public String getNazwaMiejscowosci() {
        return nazwaMiejscowosci;
    }

    public void setNazwaMiejscowosci(String nazwaMiejscowosci) {
        this.nazwaMiejscowosci = nazwaMiejscowosci;
    }

    public String getKodPocztowy() {
        return kodPocztowy;
    }

    public void setKodPocztowy(String kodPocztowy) {
        this.kodPocztowy = kodPocztowy;
    }

    public String getNazwaUlicy() {
        return nazwaUlicy;
    }

    public void setNazwaUlicy(String nazwaUlicy) {
        this.nazwaUlicy = nazwaUlicy;
    }

    public String getWojewodztwo() {
        return wojewodztwo;
    }

    public void setWojewodztwo(String wojewodztwo) {
        this.wojewodztwo = wojewodztwo;
    }

    public String getPowiat() {
        return powiat;
    }

    public void setPowiat(String powiat) {
        this.powiat = powiat;
    }

    public String getGmina() {
        return gmina;
    }

    public void setGmina(String gmina) {
        this.gmina = gmina;
    }

    public Integer getRcs() {
        return rcs;
    }

    public void setRcs(Integer rcs) {
        this.rcs = rcs;
    }

    public BigDecimal getWspolczynnikCieplaSpalania() {
        return wspolczynnikCieplaSpalania;
    }

    public void setWspolczynnikCieplaSpalania(BigDecimal wspolczynnikCieplaSpalania) {
        this.wspolczynnikCieplaSpalania = wspolczynnikCieplaSpalania;
    }

    public LocalDateTime getDataWstawienia() {
        return dataWstawienia;
    }

    public void setDataWstawienia(LocalDateTime dataWstawienia) {
        this.dataWstawienia = dataWstawienia;
    }

    public String getStatusNaStronie() {
        return statusNaStronie;
    }

    public void setStatusNaStronie(String statusNaStronie) {
        this.statusNaStronie = statusNaStronie;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRodzajGazu() {
        return rodzajGazu;
    }

    public void setRodzajGazu(String rodzajGazu) {
        this.rodzajGazu = rodzajGazu;
    }

    public String getStopienGazyfikacji() {
        return stopienGazyfikacji;
    }

    public void setStopienGazyfikacji(String stopienGazyfikacji) {
        this.stopienGazyfikacji = stopienGazyfikacji;
    }

    public String getPunktyWejscia() {
        return punktyWejscia;
    }

    public void setPunktyWejscia(String punktyWejscia) {
        this.punktyWejscia = punktyWejscia;
    }
}
