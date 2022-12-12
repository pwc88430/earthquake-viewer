package cs1302.quake;

import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;

import java.lang.Thread;


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
import javafx.collections.FXCollections;

import javafx.beans.binding.Bindings;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;


/**
 * Custom component utilized by EarthQuakeApp.
 */
public class OptionsPane extends GridPane {

    Button locate;
    DatePicker datePicker;
    Slider slider;
    Label minMagnitude;
    ComboBox<String> comboBox;
    Label selectedMagnitude;
    Label numOfQuakes;
    Label beginingDate;
    TableView<Earthquake> table;
    Label results;
    Label resultSource;

    int resultLimit;
    double minimumMagnitude;

    private Runnable runable;
    private Thread resultLoader;
    private float date;

    private EarthQuakeApp app;

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

    String[] amounts = {"1", "5", "10", "15", "20"};

    public OptionsPane(EarthQuakeApp app) {
        super();
        this.app = app;
        resultLimit = 10;
        minimumMagnitude = 5;
        date = 00000;
        locate = new Button("Locate");
        datePicker = new DatePicker();
        slider = new Slider(0.1, 9.0, 5);
        selectedMagnitude = new Label();
        selectedMagnitude.setPrefWidth(30);

        selectedMagnitude.textProperty().bind(
            Bindings.format(
                "%.2f",
                slider.valueProperty()
            )
        );

        System.out.println(slider.valueProperty());

        minMagnitude = new Label("Minimum Magnitude:");
        comboBox = new ComboBox(FXCollections.observableArrayList(amounts));
        comboBox.setValue("5");
        comboBox.setPrefWidth(40);

        // selectedMagnitude = new Label("5");
        beginingDate = new Label("Begining Date:");
        numOfQuakes = new Label("Result Limit:");
        table = new TableView<>();
        table.setPrefHeight(180);
        table.setPrefWidth(225);


        TableColumn<Earthquake,Double> magnitudeCol = new TableColumn<>("Magnitude");
        magnitudeCol.setCellValueFactory(new PropertyValueFactory<>("mag"));
        magnitudeCol.setMinWidth(120);
        TableColumn<Earthquake,String> placeCol = new TableColumn<>("Location");
        placeCol.setCellValueFactory(new PropertyValueFactory<>("place"));
        placeCol.setMinWidth(300);
        TableColumn<Earthquake,Long> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeCol.setMinWidth(100);
        TableColumn<Earthquake,String> detailCol = new TableColumn<>("More Info");
        detailCol.setCellValueFactory(new PropertyValueFactory<>("detail"));
        detailCol.setMinWidth(200);
        TableColumn<Earthquake,String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setMinWidth(84);


        table.getColumns().setAll(typeCol, magnitudeCol, placeCol, timeCol, detailCol);
        table.setTableMenuButtonVisible(true);


        results = new Label("Results:");
        resultSource = new Label("All Earthquake data provided by https://earthquake.usgs.gov");

        runable = () -> loadResults(Integer.parseInt(comboBox.getValue()),
        Double.parseDouble(selectedMagnitude.getText()), date);
        resultLoader = new Thread(runable);
        resultLoader.setDaemon(true);
        locate.setOnAction(event -> new Thread(resultLoader).start());

        this.setPadding(new Insets(10, 10, 10, 10));
        this.setAlignment(Pos.TOP_LEFT);
        this.setVgap(5);
        this.setHgap(5);

        this.setPrefHeight(400);
        this.setPrefWidth(250);
        this.add(locate, 0, 0);

        this.add(slider, 0, 2, 2, 1);
        this.add(minMagnitude, 0, 1, 2, 1);
        this.add(selectedMagnitude, 1, 1, 2, 1);

        this.add(beginingDate, 0, 4);
        this.add(datePicker, 0, 5);

        this.add(numOfQuakes, 0, 6);
        this.add(comboBox, 1, 6);

        this.add(results, 0, 13);
        this.add(table, 0, 14, 3, 1);

        this.add(resultSource, 0, 15);
    } // constructor


    /**
     * Method to get results form earthquake Api.
     */
    private void loadResults(int resultLimit, double minMagnitude, float date) {

        try {

        HttpRequest request = buildRequest(resultLimit, minMagnitude, date);
        HttpResponse<String> response = HTTP_CLIENT
            .send(request, BodyHandlers.ofString());

        String geojson = response.body();

        EQAPIResponse apiResponse = GSON
            .fromJson(geojson, EQAPIResponse.class);

        Earthquake[] earthquakes = new Earthquake[Integer.parseInt(apiResponse.metadata.count)];

        for (int i = 0; i < earthquakes.length; i++) {
            double mag = apiResponse.features[i].properties.mag;
            String place = apiResponse.features[i].properties.place;
            long time = apiResponse.features[i].properties.time;
            String detail = apiResponse.features[i].properties.detail;
            String type = apiResponse.features[i].properties.type;
            double longitude = apiResponse.features[i].geometry.coordinates[0];
            double latitude = apiResponse.features[i].geometry.coordinates[1];

            earthquakes[i] = new Earthquake(mag, place, time, detail, type, longitude, latitude);
        } // for

        table.setItems(FXCollections.observableArrayList(earthquakes));
        table.refresh();


        app.displayPointers(earthquakes);

        } catch(Exception e) {

            System.out.println(e);

        } // try-catch


    } // loadResults


        /**
     * BuildRequest builds an HTTP request object.
     */
    public HttpRequest buildRequest(int resultLimit, double minMagnitude, float Date) {

        String uri = EARTHQUAKE_API + "/" + METHOD + "?" + FORMAT +
            "&minmagnitude=" + minMagnitude +
            "&eventtype=earthquake&starttime=2022-01-01&limit=" + resultLimit;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .build();

        System.out.println("locating..." + uri);



        return request;

    } // buildRequest



} // OptionsPane
