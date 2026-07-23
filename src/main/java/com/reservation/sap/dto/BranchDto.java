package com.reservation.sap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SAP snapshot row for table {@code oddzial}. JSON keys match MySQL column names.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchDto {

    private Integer id;

    @JsonProperty("oddzial")
    private String name;

    @JsonProperty("nazwa_miejscowosci")
    private String localityName;

    @JsonProperty("kod_pocztowy")
    private String postalCode;

    @JsonProperty("nazwa_ulicy")
    private String streetName;

    @JsonProperty("wojewodztwo")
    private String province;

    @JsonProperty("powiat")
    private String county;

    @JsonProperty("gmina")
    private String commune;

    private Integer rcs;

    @JsonProperty("wspolczynnik_ciepla_spalania")
    private BigDecimal combustionHeatCoefficient;

    @JsonProperty("data_wstawienia")
    private LocalDateTime insertedAt;

    @JsonProperty("status_na_stronie")
    private String pageStatus;

    @JsonProperty("Telefon")
    private String phone;

    private String email;

    @JsonProperty("Rodzaj_gazu")
    private String gasType;

    @JsonProperty("Stopien_gazyfikacji")
    private String gasificationDegree;

    @JsonProperty("Punkty_wejscia")
    private String entryPoints;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalityName() {
        return localityName;
    }

    public void setLocalityName(String localityName) {
        this.localityName = localityName;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCommune() {
        return commune;
    }

    public void setCommune(String commune) {
        this.commune = commune;
    }

    public Integer getRcs() {
        return rcs;
    }

    public void setRcs(Integer rcs) {
        this.rcs = rcs;
    }

    public BigDecimal getCombustionHeatCoefficient() {
        return combustionHeatCoefficient;
    }

    public void setCombustionHeatCoefficient(BigDecimal combustionHeatCoefficient) {
        this.combustionHeatCoefficient = combustionHeatCoefficient;
    }

    public LocalDateTime getInsertedAt() {
        return insertedAt;
    }

    public void setInsertedAt(LocalDateTime insertedAt) {
        this.insertedAt = insertedAt;
    }

    public String getPageStatus() {
        return pageStatus;
    }

    public void setPageStatus(String pageStatus) {
        this.pageStatus = pageStatus;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGasType() {
        return gasType;
    }

    public void setGasType(String gasType) {
        this.gasType = gasType;
    }

    public String getGasificationDegree() {
        return gasificationDegree;
    }

    public void setGasificationDegree(String gasificationDegree) {
        this.gasificationDegree = gasificationDegree;
    }

    public String getEntryPoints() {
        return entryPoints;
    }

    public void setEntryPoints(String entryPoints) {
        this.entryPoints = entryPoints;
    }
}
