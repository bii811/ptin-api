package com.example.ptin.shared.mediation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Inner object of the {@code ResTaxRIS} response envelope. {@code TinInfo}/{@code Address} are absent on errors. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResTaxRis(
        // Result - CD / MSG / CNT
        @JsonProperty("Result") Result result,
        // TinInfo - issued TIN and confirmed names (absent on error)
        @JsonProperty("TinInfo") TinInfo tinInfo,
        // Address - 1..n address records (callAddress only; always a list)
        @JsonProperty("Address") List<Address> address) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            // CD - Result Code (see TaxRIS "Return codes and messages"; "000" = success)
            @JsonProperty("CD") String cd,
            // MSG - Result Message - detail message of result
            @JsonProperty("MSG") String msg,
            // CNT - Result Count - totally processed count (fixed to '1' for TIN services)
            @JsonProperty("CNT") String cnt) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TinInfo(
            // TIN - Taxpayer Identification Number
            @JsonProperty("TIN") String tin,
            // TAXR_GV_NM - Given Name
            @JsonProperty("TAXR_GV_NM") String givenName,
            // TAXR_FAM_NM - Family Name
            @JsonProperty("TAXR_FAM_NM") String familyName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Address(
            // ADDR_LVL_TP - Address Level
            @JsonProperty("ADDR_LVL_TP") String levelType,
            // ADDR_CD - Address Code
            @JsonProperty("ADDR_CD") String code,
            // ADDR_CD_NM - Address Name
            @JsonProperty("ADDR_CD_NM") String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Envelope(@JsonProperty("ResTaxRIS") ResTaxRis res) {
    }
}
