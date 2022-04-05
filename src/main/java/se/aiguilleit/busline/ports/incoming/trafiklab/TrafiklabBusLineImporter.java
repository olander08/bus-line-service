package se.aiguilleit.busline.ports.incoming.trafiklab;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import se.aiguilleit.busline.domain.BusLine;
import se.aiguilleit.busline.domain.BusLineImportException;
import se.aiguilleit.busline.domain.BusLineImporter;
import se.aiguilleit.busline.domain.Stop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TrafiklabBusLineImporter implements BusLineImporter {

    public static final String QUERY_API_KEY = "key";
    public static final String QUERY_MODEL = "model";
    public static final String QUERY_MODEL_STOP_POINT_VALUE = "stop";
    public static final String QUERY_MODEL_JOURNEY_PATTERN_POINT_ON_LINE_VALUE = "jour";
    private static final String STOP_AREA_TYPE_BUS = "BUSTERM";

    private String apiKey;
    private String endpoint;
    private RestTemplate restTemplate;
    private String url;
    private HttpEntity<?> entity;


    public TrafiklabBusLineImporter(
            @Value("${trafiklab.stops-and-lines-2.api-key}") String apiKey,
            @Value("${trafiklab.stops-and-lines-2.endpoint}") String endpoint,
            RestTemplate restTemplate
    ) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.restTemplate = restTemplate;
        this.url = getUrl();
        this.entity = getHttpEntity();
    }


    @Override
    public List<BusLine> getBusLines() {
        Map<Integer, StopDTO> stopsByNumber = getStops().stream()
                .filter(TrafiklabBusLineImporter::isBusStop)
                .collect(Collectors.toMap(StopDTO::stopPointNumber, Function.identity()));

        Map<Integer, List<LineStopDTO>> busLineStops = getPointsOnLine().stream()
                .filter(l -> isBusStopMapping(stopsByNumber, l.stopPointNumber()))
                .collect(Collectors.groupingBy(LineStopDTO::lineNumber));

        return busLineStops.entrySet().stream()
                .map(entry -> toBusLine(entry, stopsByNumber))
                .collect(Collectors.toList());
    }


    private List<StopDTO> getStops() {
        Map<String, String> params = new HashMap<>();
        params.put("model", QUERY_MODEL_STOP_POINT_VALUE);
        params.put("key", apiKey);

        TrafikLabResponseDTO<StopDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<TrafikLabResponseDTO<StopDTO>>() {},
                params
        ).getBody();

        if (response == null || response.responseData() == null) {
            throw new BusLineImportException("Response from Trafiklab was null for StopPoint");
        }

        return response.responseData().result();
    }


    private List<LineStopDTO> getPointsOnLine() {
        Map<String, String> params = new HashMap<>();
        params.put("model", QUERY_MODEL_JOURNEY_PATTERN_POINT_ON_LINE_VALUE);
        params.put("key", apiKey);

        TrafikLabResponseDTO<LineStopDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<TrafikLabResponseDTO<LineStopDTO>>() {},
                params
        ).getBody();

        if (response == null || response.responseData() == null) {
            throw new BusLineImportException("Response from Trafiklab was null for JourneyPatternPointOnLine");
        }

        return response.responseData().result();
    }


    private static boolean isBusStop(StopDTO stop) {
        return STOP_AREA_TYPE_BUS.equals(stop.stopAreaTypeCode());
    }


    private static boolean isBusStopMapping(Map<Integer, StopDTO> stopsByNumber, int stopPointNumber) {
        return stopsByNumber.containsKey(stopPointNumber);
    }


    private static BusLine toBusLine(Map.Entry<Integer, List<LineStopDTO>> entry, Map<Integer, StopDTO> stopsByNumber) {
        Set<Stop> stops = toStops(entry, stopsByNumber);
        return new BusLine(entry.getKey(), stops);
    }

    private static Set<Stop> toStops(Map.Entry<Integer, List<LineStopDTO>> entry, Map<Integer, StopDTO> stopsByNumber) {
        return entry.getValue().stream()
                .map(l -> {
                    StopDTO stop = stopsByNumber.get(l.stopPointNumber());
                    return new Stop(
                            stop.stopPointNumber(),
                            stop.stopAreaNumber(),
                            stop.stopPointName()
                    );
                })
                .collect(Collectors.toSet());
    }


    private String getUrl() {
        return UriComponentsBuilder.fromHttpUrl(endpoint)
            .queryParam(QUERY_MODEL, "{model}")
            .queryParam(QUERY_API_KEY, "{key}")
            .encode()
            .toUriString();
    }


    private static HttpEntity<?> getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
        return new HttpEntity<>(headers);
    }
}

record TrafikLabResponseDTO<T>(
        @JsonProperty("ResponseData") ResponseDataDTO<T> responseData
) {}
record ResponseDataDTO<T>(
        @JsonProperty("Result") List<T> result
) {}
record StopDTO(
        @JsonProperty("StopPointNumber") int stopPointNumber,
        @JsonProperty("StopPointName") String stopPointName,
        @JsonProperty("StopAreaNumber") int stopAreaNumber,
        @JsonProperty("StopAreaTypeCode") String stopAreaTypeCode
) {}
record LineStopDTO(
        @JsonProperty("LineNumber") int lineNumber,
        @JsonProperty("JourneyPatternPointNumber") int stopPointNumber
) {}