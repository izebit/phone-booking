package ru.izebit.mobile.booking.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.izebit.mobile.booking.dto.Detail;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@Slf4j
public class PhoneInformationService {
    private static final List<Entry<String, String>> REQUIRED_FIELDS = List.of(
            new SimpleEntry<>("Network", "Network"),
            new SimpleEntry<>("TwoG", "2G"),
            new SimpleEntry<>("ThreeG", "3G"),
            new SimpleEntry<>("FourG", "4G")
    );
    private final ConcurrentMap<String, List<Detail>> cachedValues = new ConcurrentHashMap<>();

    @Value("${cell-phone-dataset-api.rest-api-key}")
    private String restApiKey;
    @Value("${cell-phone-dataset-api.application-id}")
    private String applicationId;

    public List<Detail> findFor(String phoneTitle) {
        String[] values = phoneTitle.split(" ", 2);
        var response = getResponseWith(values[0], values[1]);
        var details = parseResponse(response);
        if (details.isEmpty())
            return cachedValues.getOrDefault(phoneTitle, Collections.emptyList());

        cachedValues.put(phoneTitle, details);
        return details;
    }

    @SneakyThrows
    private static List<Detail> parseResponse(JSONObject response) {
        if (response == null || !response.has("results"))
            return Collections.emptyList();
        var results = response.getJSONArray("results");
        if (results.length() != 1)
            return Collections.emptyList();

        final var responseObject = results.getJSONObject(0);
        return REQUIRED_FIELDS.stream()
                .filter(e -> responseObject.has(e.getKey()))
                .map(entry -> {
                    var jsonField = entry.getKey();
                    var mappedField = entry.getValue();
                    return new Detail(mappedField, responseObject.getString(jsonField));
                })
                .toList();
    }

    @SneakyThrows
    private JSONObject getResponseWith(final String brand, final String model) {
        var parameters = "?limit=1" +
                "&keys=Brand,Model,Network,Network_Speed,TwoG,ThreeG,FourG" +
                "&where=" + URLEncoder.encode("{" +
                "    \"Brand\": \"" + brand + "\"," +
                "    \"Model\": {" +
                "        \"$regex\": \".*" + model + ".*\"" +
                "    }" +
                "}", StandardCharsets.UTF_8);

        var request = HttpRequest.newBuilder()
                .uri(new URI("https://parseapi.back4app.com/classes/Dataset_Cell_Phones_Model_Brand" + parameters))
                .timeout(Duration.of(10, SECONDS))
                .header("X-Parse-Application-Id", applicationId)
                .header("X-Parse-REST-API-Key", restApiKey)
                .GET()
                .build();

        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new JSONObject(response.body());
        } catch (IOException | InterruptedException e) {
            log.error("error while connecting to api", e);
            return new JSONObject();
        }
    }
}
