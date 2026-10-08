package com.example.ptin.shared.mediation;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Single entry point to the TaxRIS mediation gateway. Every request goes through {@link #post} so it
 * is audited in {@code taxris_api_call_logs} (HASH_KEY redacted), including transport failures.
 */
@Component
public class TaxRisClient {

    private static final String ISSUE_INDIVIDUAL_TIN_PATH = "/mediate/TaxRIS/issueIndividualTin/ReqTinInfo";
    private static final String MANAGE_PTIN_PATH = "/mediate/TaxRIS/managePTinInformation/ReqTinInfo";
    private static final String CALL_ADDRESS_PATH = "/mediate/TaxRIS/callAddress/ReqADD";
    private static final String SUCCESS_CODE = "000";
    private static final String REQUEST_HEADERS = "Content-Type: application/json";

    /** Raw outcome of one HTTP exchange; {@code status} is null when the call never got a response. */
    public record Exchange(Integer status, String body, String error) {
    }

    private final RestClient restClient;
    private final TaxRisMediationProperties properties;
    private final ObjectMapper objectMapper;
    private final TaxRisApiCallLogWriter logWriter;

    TaxRisClient(RestClient.Builder restClientBuilder, TaxRisMediationProperties properties,
            ObjectMapper objectMapper, TaxRisApiCallLogWriter logWriter) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.logWriter = logWriter;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.connectTimeoutMs());
        requestFactory.setReadTimeout((int) properties.readTimeoutMs());
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).requestFactory(requestFactory).build();
    }

    /** Issues a TIN for an individual. */
    public TaxRisResult<ResTaxRis.TinInfo> issueIndividualTin(ReqTinInfo request, CallContext ctx) {
        return call(ISSUE_INDIVIDUAL_TIN_PATH, "ReqTinInfo", request.withAuth(properties.hashKey(), null), ctx,
                ResTaxRis::tinInfo);
    }

    /** Issues a TIN on behalf of LMIS; {@code request.laboId()} is required, SYS is added here. */
    public TaxRisResult<ResTaxRis.TinInfo> managePTinInformation(ReqTinInfo request, CallContext ctx) {
        return call(MANAGE_PTIN_PATH, "ReqTinInfo", request.withAuth(properties.hashKey(), properties.sys()), ctx,
                ResTaxRis::tinInfo);
    }

    /** {@code actCd}: P = province, D = district (then {@code addrCd} is the parent code). */
    public TaxRisResult<List<ResTaxRis.Address>> callAddress(String actCd, String addrCd, CallContext ctx) {
        Map<String, Object> depot = new LinkedHashMap<>();
        depot.put("HASH_KEY", properties.hashKey());
        depot.put("ACT_CD", actCd);
        depot.put("ADDR_CD", addrCd == null ? "" : addrCd);
        return call(CALL_ADDRESS_PATH, "ReqADD", depot, ctx,
                res -> res.address() == null ? List.of() : res.address());
    }

    private <T> TaxRisResult<T> call(String path, String depotName, Object depot, CallContext ctx,
            Function<ResTaxRis, T> dataExtractor) {
        Exchange exchange = post(path, Map.of(depotName, depot), ctx);
        if (exchange.status() == null) {
            return TaxRisResult.failure(null, exchange.error());
        }
        ResTaxRis res;
        try {
            res = objectMapper.readValue(exchange.body(), ResTaxRis.Envelope.class).res();
        } catch (RuntimeException e) {
            return TaxRisResult.failure(null, "Unparseable TaxRIS response (HTTP " + exchange.status() + ")");
        }
        if (res == null || res.result() == null) {
            return TaxRisResult.failure(null, "TaxRIS response has no Result (HTTP " + exchange.status() + ")");
        }
        String code = res.result().cd();
        boolean ok = exchange.status() / 100 == 2 && SUCCESS_CODE.equals(code);
        return ok ? new TaxRisResult<>(true, code, res.result().msg(), dataExtractor.apply(res))
                : TaxRisResult.failure(code, res.result().msg());
    }

    /** POSTs {@code body} as JSON to {@code path}, logs the exchange, never throws. */
    public Exchange post(String path, Object body, CallContext ctx) {
        Instant calledAt = Instant.now();
        String url = properties.baseUrl() + path;
        String requestJson = null;
        Exchange exchange;
        try {
            requestJson = objectMapper.writeValueAsString(body);
            exchange = restClient.post().uri(path).contentType(MediaType.APPLICATION_JSON).body(requestJson)
                    .exchange((req, res) -> new Exchange(res.getStatusCode().value(),
                            new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8), null));
        } catch (RestClientException | tools.jackson.core.JacksonException e) {
            exchange = new Exchange(null, null, e.getMessage());
        }
        recordCall(ctx, url, requestJson, exchange, calledAt);
        return exchange;
    }

    private void recordCall(CallContext ctx, String url, String requestJson, Exchange exchange, Instant calledAt) {
        String code = null;
        String message = exchange.error();
        if (exchange.body() != null) {
            try {
                // Result sits under ResTaxRIS for the guide's services and at the root for createPfmFromCRSDtl.
                JsonNode root = objectMapper.readTree(exchange.body());
                JsonNode result = root.path("ResTaxRIS").path("Result");
                result = result.isMissingNode() ? root.path("Result") : result;
                code = result.path("CD").asString(null);
                message = result.path("MSG").asString(message);
            } catch (RuntimeException e) {
                message = "Unparseable response (HTTP " + exchange.status() + ")";
            }
        }
        logWriter.write(new TaxRisApiCallLog(ctx, url, REQUEST_HEADERS, redact(requestJson), exchange.body(), code,
                message, calledAt));
    }

    private String redact(String json) {
        String key = properties.hashKey();
        return json == null || key == null || key.isBlank() ? json : json.replace(key, "***");
    }
}
