package cs1302.quake;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.Integer;

/**
 * EarthquakeApiTester is a class to test building, sending, recieving, and parsing
 * requests from earthquake api.
 *
 */
public class EarthquakeApiTesting {

    /** HTTP client **/
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private static final String EARTHQUAKE_API = "https://earthquake.usgs.gov/fdsnws/event/1";

    private static final String FORMAT = "format=geojson";

    private static final String METHOD = "query";


    public static void main(String[] args) {

        try {

            HttpRequest request = buildRequest();

            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());

            String geojson = response.body();

            EQAPIResponse apiResponse = GSON
                .fromJson(geojson, EQAPIResponse.class);

            System.out.println(apiResponse.metadata.count);

            for (int i = 0; i < Integer.parseInt(apiResponse.metadata.count); i++) {
                System.out.println(apiResponse.features[i].properties.type);
                System.out.println(apiResponse.features[i].properties.mag);
                System.out.println(apiResponse.features[i].properties.place);
                System.out.println(apiResponse.features[i].properties.time);
            }

            // System.out.println(geojson);
            // System.out.println(request.uri().toString());

        } catch(Exception e) {

            System.out.println(e);

        } // try-catch


    } // main

    /**
     * BuildRequest builds an HTTP request object.
     */
    public static HttpRequest buildRequest() {

        String query = "/" + METHOD + "?" + FORMAT + "&minmagnitude=5&eventtype=earthquake";

        // String uri = EARTHQUAKE_API + query;

        String uri = EARTHQUAKE_API + "/" + METHOD + "?" + FORMAT +
            "&minmagnitude=5&eventtype=earthquake&starttime=2022-01-01&limit=10";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .build();

        return request;

    } // buildRequest


} // EarthQuakeApiTester
