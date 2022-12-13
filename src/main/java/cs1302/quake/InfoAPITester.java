package cs1302.quake;

import java.net.http.HttpClient;
import java.net.URI;
import java.net.URL;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import javafx.collections.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Class to test API for retrieving more earthquake details.
 */
public class InfoAPITester {

    public static HttpRequest request;
    public static HttpResponse<String> response;
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();


    /**
     * Main method.
     * @param args command line arguments
     */
    public static void main(String[] args) {


        try {
            request = createRequest();

            response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());

            String jsonString = response.body();


            InfoAPIResponse apiResponse = GSON
                .fromJson(jsonString, InfoAPIResponse.class);

            System.out.println("Title: " + apiResponse.value[0].title);
            System.out.println("Info Url: " + apiResponse.value[0].url);
            System.out.println("Description: " + apiResponse.value[0].description);
            System.out.println("Image Url: " + apiResponse.value[0].image.url);
        } catch (Exception e) {
            System.out.println(e);
        }

    } // main

    /**
     * Method to create a new HttpRequest object.
     * @return a reference to a new HttpRequest object
     */
    public static HttpRequest createRequest() {


        String location = "fiji";
        String time = "2014";

        String uri = "https://contextualwebsearch-websearch-v1.p.rapidapi.com/api/Search/" +
            "WebSearchAPI?q=earthquakenewstoday%20earthquake%20" + location +
            "%20" + time + "&pageNumber=1&pageSize=1&autoCorrect=true&safeSearch=true";


        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .header("X-RapidAPI-Key", "c88637b20emshc118db30b941d9fp1345bajsnd02a670fa0e4")
            .header("X-RapidAPI-Host", "contextualwebsearch-websearch-v1.p.rapidapi.com")
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .build();
        return request;

    } // createRequest



} // InfoAPITester
