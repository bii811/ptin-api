package com.example.ptin.shared.mediation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class ResTaxRisTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    private ResTaxRis parse(String json) {
        return mapper.readValue(json, ResTaxRis.Envelope.class).res();
    }

    @Test
    void parsesTinSuccess() {
        ResTaxRis res = parse("""
                {"ResTaxRIS":{"Result":{"CD":"000","MSG":"Success","CNT":"1"},
                 "TinInfo":{"TIN":"100000000001","TAXR_GV_NM":"A","TAXR_FAM_NM":"B"}}}""");
        assertEquals("000", res.result().cd());
        assertEquals("100000000001", res.tinInfo().tin());
    }

    @Test
    void errorResponseHasNoTinInfo() {
        ResTaxRis res = parse("""
                {"ResTaxRIS":{"Result":{"CD":"E01","MSG":"bad","CNT":"0"}}}""");
        assertEquals("E01", res.result().cd());
        assertNull(res.tinInfo());
    }

    @Test
    void singleAddressIsStillAList() {
        ResTaxRis res = parse("""
                {"ResTaxRIS":{"Result":{"CD":"000","MSG":"ok","CNT":"1"},
                 "Address":[{"ADDR_LVL_TP":"P","ADDR_CD":"01","ADDR_CD_NM":"x"}]}}""");
        assertEquals(1, res.address().size());
    }

    @Test
    void bodyIsWrappedAndOptionalLaborFieldsOmitted() {
        var req = new ReqTinInfo("k", null, null, "G", "F", "M", "LA", "19900101", null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null).withAuth("k", null);
        String json = mapper.writeValueAsString(java.util.Map.of("ReqTinInfo", req));
        org.junit.jupiter.api.Assertions.assertTrue(json.startsWith("{\"ReqTinInfo\":{\"HASH_KEY\":\"k\""));
        org.junit.jupiter.api.Assertions.assertFalse(json.contains("LABO_ID") || json.contains("\"SYS\""));
    }
}
