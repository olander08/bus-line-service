package se.aiguilleit.busline;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;
import se.aiguilleit.busline.application.BusLineService;
import se.aiguilleit.busline.ports.incoming.rest.BusLineDTO;
import se.aiguilleit.busline.ports.incoming.rest.BusLinesDTO;
import se.aiguilleit.busline.ports.incoming.rest.StopDTO;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static se.aiguilleit.busline.ports.incoming.trafiklab.TrafiklabBusLineImporter.QUERY_MODEL_JOURNEY_PATTERN_POINT_ON_LINE_VALUE;
import static se.aiguilleit.busline.ports.incoming.trafiklab.TrafiklabBusLineImporter.QUERY_MODEL_STOP_POINT_VALUE;

@ActiveProfiles("test")
@AutoConfigureWireMock(port = 9999)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BusLineIntegrationTest {

    @Value("${trafiklab.stops-and-lines-2.api-key}")
    private String trafiklabApiKey;

    @Value("classpath:trafiklab/journeyPatternPointOnLineResponse.json")
    private Resource pointOnLineResponseResource;

    @Value("classpath:trafiklab/stopPointResponse.json")
    private Resource stopPointResponseResource;

    @LocalServerPort
    private int port;

    @Autowired
    private BusLineService busLineService;

    private static final StopDTO STADSHAGSPLAN;
    private static final StopDTO JOHAN_BERGS_PLAN;
    private static final StopDTO JOHAN_BERGS_PLAN_2;
    private static final StopDTO ARBETARGATAN;


    static {
        STADSHAGSPLAN = new StopDTO(10001, 10001, "Stadshagsplan");
        JOHAN_BERGS_PLAN = new StopDTO(10002, 10002, "John Bergs plan");
        JOHAN_BERGS_PLAN_2 = new StopDTO(10003, 10002, "John Bergs plan 2");
        ARBETARGATAN = new StopDTO(10006, 10006, "Arbetargatan");
    }


    @BeforeEach
    public void setupRestAssured() {
        RestAssured.port = port;
    }


    @Test
    public void testGetBusLines() throws IOException {
        stubFor(get(urlPathEqualTo("/stop-lines.json"))
                .withHeader(HttpHeaders.ACCEPT_ENCODING, equalTo("gzip, deflate"))
                .withQueryParam("key", equalTo(trafiklabApiKey))
                .withQueryParam("model", equalTo(QUERY_MODEL_STOP_POINT_VALUE))
                .willReturn(okJson(asJson(stopPointResponseResource))));

        stubFor(get(urlPathEqualTo("/stop-lines.json"))
                .withHeader(HttpHeaders.ACCEPT_ENCODING, equalTo("gzip, deflate"))
                .withQueryParam("key", equalTo(trafiklabApiKey))
                .withQueryParam("model", equalTo(QUERY_MODEL_JOURNEY_PATTERN_POINT_ON_LINE_VALUE))
                .willReturn(okJson(asJson(pointOnLineResponseResource))));

        busLineService.importBusLines();

        List<BusLineDTO> busLines = RestAssured
                .given()
                .when()
                .get("/api/bus-lines")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .as(BusLinesDTO.class)
                .busLines();

        assertThat(busLines, notNullValue());
        assertThat(busLines, hasSize(3));

        assertBusLine(busLines, 1,
                Arrays.asList(STADSHAGSPLAN, JOHAN_BERGS_PLAN, JOHAN_BERGS_PLAN_2, ARBETARGATAN));

        assertBusLine(busLines, 2,
                Arrays.asList(STADSHAGSPLAN, JOHAN_BERGS_PLAN, JOHAN_BERGS_PLAN_2, ARBETARGATAN));

        assertBusLine(busLines, 3,
                Arrays.asList(STADSHAGSPLAN, ARBETARGATAN));
    }


    private void assertBusLine(List<BusLineDTO> actualBusLines, int lineNumber, List<StopDTO> expectedStops) {
        Optional<BusLineDTO> busLineOpt = actualBusLines.stream()
                .filter(b -> b.lineNumber() == lineNumber)
                .findFirst();

        assertThat(busLineOpt.isPresent(), Matchers.equalTo(true));

        BusLineDTO actualBusLine = busLineOpt.get();

        assertThat(actualBusLine.stops(), containsInAnyOrder(expectedStops.toArray()));
    }


    private String asJson(Resource resource) throws IOException {
        return StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
    }
}
