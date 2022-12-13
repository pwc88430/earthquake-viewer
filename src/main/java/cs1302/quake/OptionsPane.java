package cs1302.quake;

import java.io.*;
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
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.control.TextField;
import java.lang.Thread;
import javafx.scene.layout.StackPane;
import java.time.ZonedDateTime;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Rectangle2D;
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
import javafx.scene.text.Font;

import javafx.beans.binding.Bindings;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;

import java.time.LocalDate;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.collections.ObservableList;
import javafx.collections.*;

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
    ComboBox<String> regionSelection;
    Label regionFilter;
    Label credit;

    int resultLimit;
    double minimumMagnitude;
    LocalDate defaultDate;
    TableViewSelectionModel<Earthquake> selectionModel;

    private Runnable runable, moreInfo;
    private Thread resultLoader;
    private float date;

    private EarthQuakeApp app;

    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private static Image zoomedImage =
        new Image("file:resources/World_location_map_(equirectangular_180).svg-2.png");

    private static final String EARTHQUAKE_API = "https://earthquake.usgs.gov/fdsnws/event/1";

    private static final String FORMAT = "format=geojson";

    private static final String METHOD = "query";

    String[] amounts = {"1", "5", "10", "15", "20"};
    String[] regions = {"Northern hemisphere", "Southern hemisphere",
        "Eastern hemisphere", "Western hemisphere", "All regions"};

    /**
     * A constructor for an OptionsPane object.
     * @param app is a reference to the EarthQuakeApp that created this optionsPane
     */
    public OptionsPane(EarthQuakeApp app) {
        super();
        this.app = app;
        resultLimit = 10;
        minimumMagnitude = 5;
        date = 00000;
        locate = new Button("Locate Earthquakes");
        datePicker = new DatePicker();
        defaultDate = LocalDate.of(2020, 1, 1);
        datePicker.setValue(defaultDate);
        slider = new Slider(0.1, 9.0, 5);
        slider.setPrefWidth(60);
        selectedMagnitude = new Label();
        selectedMagnitude.setPrefWidth(50);
        selectedMagnitude.textProperty().bind(
            Bindings.format(
                "   %.2f",
                slider.valueProperty()
            )
        );
        credit = new Label("Earthquake data provided by https://earthquake.usgs.gov/");
        credit.setFont(new Font(10));
        regionFilter = new Label("Filter by region:");
        regionSelection = new ComboBox<>(FXCollections.observableArrayList(regions));
        regionSelection.setValue("All regions");
        minMagnitude = new Label("Minimum Magnitude:  ");
        comboBox = new ComboBox<>(FXCollections.observableArrayList(amounts));
        comboBox.setValue("5");
        comboBox.setPrefWidth(55);

        beginingDate = new Label("Begining Date:");
        numOfQuakes = new Label("Result Limit:");
        table = newTableView();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(43);
        this.getColumnConstraints().addAll(col1);

        results = new Label("Results:");
        resultSource = new Label("All Earthquake data provided by https://earthquake.usgs.gov");

        runable = () -> loadResults(Integer.parseInt(comboBox.getValue()),
        Double.parseDouble(selectedMagnitude.getText()), date, regionSelection.getValue(),
        datePicker.getValue().toString());
        resultLoader = new Thread(runable);
        resultLoader.setDaemon(true);
        locate.setOnAction(event -> new Thread(resultLoader).start());

        this.addElements();

    } // constructor


    /**
     * Method to get results form earthquake Api.
     * @param resultLimit is the maximum number of results
     * @param minMagnitude is the minmum magnitude
     * @param date is the begining date
     * @param region is the region constraint
     * @param starttime i sthe begining date
     */
    private void loadResults(int resultLimit, double minMagnitude, float date,
        String region, String starttime) {
        try {
            HttpRequest request = buildRequest(resultLimit, minMagnitude, date, region, starttime);
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
                earthquakes[i] =
                    new Earthquake(mag, place, time, detail, type, longitude, latitude);
            } // for
            ObservableList<Earthquake> earthquakeList =
                FXCollections.observableArrayList(earthquakes);
            table.setItems(earthquakeList);
            table.refresh();
            app.displayPointers(earthquakes);
            earthquakeList = selectionModel.getSelectedItems();
            earthquakeList.addListener(
                new ListChangeListener<Earthquake>() {
                    @Override
                    public void onChanged(
                        Change<? extends Earthquake> change) {
                        System.out.println(
                            "Selection changed: " + change.getList());
                        OptionsPane.getMoreInfo(change.getList().get(0));
                    }
                });
        } catch (Exception e) {
            System.out.println(e);
        } // try-catch
    } // loadResults

    /**
     * BuildRequest builds an HTTP request object.
     * @param resultLimit is the maximum number of results
     * @param minMagnitude is the minmum magnitude
     * @param date is the begining date
     * @param region is the region constraint
     * @param starttime i sthe begining date
     * @return a new HttpRequest object
     */
    public HttpRequest buildRequest(int resultLimit, double minMagnitude,
        float date, String region, String starttime) {
        if (starttime == null) {
            starttime = "";
        } else {
            starttime = "&starttime=" + starttime;
        } // if-else
        if (region.equals("All regions")) {
            region = "";
        } else if (region.equals("Northern hemisphere")) {
            region = "&minlatitude=0";
        } else if (region.equals("Southern hemisphere")) {
            region = "&maxlatitude=0";
        } else if (region.equals("Eastern hemisphere")) {
            region = "&minlongitude=0";
        } else {
            region = "&maxlongitude=0";
        } // if-else
        String uri = EARTHQUAKE_API + "/" + METHOD + "?" + FORMAT +
                "&minmagnitude=" + minMagnitude +
                "&eventtype=earthquake&limit=" +
            resultLimit + region + starttime;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .build();
        return request;
    } // buildRequest

    /**
     * Method creates a popupWindow with more information.
     * @param earthquake is an Earthquake object
     */
    public static void getMoreInfo(Earthquake earthquake) {
        Label time = new Label("Time: " + earthquake.time);
        Label longitude = new Label("Longitude: " + earthquake.longitude);
        Label latitude = new Label("Latitude: " + earthquake.latitude);
        Button back = new Button("Back to map");
        ImageView zoom = new ImageView(zoomedImage);
        ImageView pointer = new ImageView("file:resources/Location_pointer.png");
        pointer.setFitHeight(100);
        pointer.setFitWidth(100);
        double x = 475 + earthquake.longitude * 3.55;
        double y = 210 + earthquake.latitude * -3.55;
        zoom.setViewport(new Rectangle2D(x,y, 330, 220));
        StackPane pane = new StackPane(zoom, pointer);
        pane.setPrefSize(330, 220);
        Label credit = new Label("Details provided by www.earthquakenewstoday.com");
        credit.setFont(new Font(10));
        try {
            HttpRequest request = newRequest(earthquake);
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException();
            }
            InfoAPIResponse apiResponse = GSON.fromJson(response.body(), InfoAPIResponse.class);
            Label title = new Label("Event: " + apiResponse.value[0].title);
            title.setWrapText(true);
            Label info = new Label(apiResponse.value[0].description);
            info.setWrapText(true);
            ImageView extraImage = new ImageView(new Image(apiResponse.value[0].image.url));
            extraImage.setFitHeight(150);
            extraImage.setFitWidth(150);
            TextField url = new TextField(apiResponse.value[0].url);
            url.setEditable(false);
            Stage dialog = new Stage();
            GridPane grid = new GridPane();
            Scene dialogScene = new Scene(grid, 350, 720);
            Runnable closeInfo = () -> dialog.close();
            formatStage(dialog, dialogScene);
            back.setOnAction(event -> closeInfo.run());
            grid.setPadding(new Insets(10, 10, 10, 10));
            grid.setAlignment(Pos.TOP_LEFT);
            grid.setVgap(5);
            grid.setHgap(5);
            grid.add(pane, 0, 0);
            grid.add(title, 0, 1);
            grid.add(time, 0, 2);
            grid.add(longitude, 0, 3);
            grid.add(latitude, 0, 4);
            grid.add(info, 0, 6);
            grid.add(url, 0, 7);
            grid.add(extraImage, 0, 8);
            grid.add(back, 0, 9);
            grid.add(credit, 0, 10);
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load more information.");
            alert.setContentText(e.toString());
            alert.showAndWait();
        }
    } // getMoreInfo

    /**
     * Method newRequest creates a new HttpRequest object.
     * @param earthquake represents an Earthquake object
     * @return a HttpRequest object
     */
    private static HttpRequest newRequest(Earthquake earthquake) {
        String location = earthquake.place.replaceAll(" ", "%20").replaceAll(",", "");
        String time = String.valueOf(earthquake.time);
        String uri = "https://contextualwebsearch-websearch-v1.p.rapidapi.com/api/Search/" +
            "WebSearchAPI?q=earthquakenewstoday%20earthquake%20" + location + "%20" + time +
            "&pageNumber=1&pageSize=1&autoCorrect=true&safeSearch=true";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .header("X-RapidAPI-Key", "c88637b20emshc118db30b941d9fp1345bajsnd02a670fa0e4")
            .header("X-RapidAPI-Host", "contextualwebsearch-websearch-v1.p.rapidapi.com")
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .build();
        return request;
    } // newRequest

    /**
     * Method to create a formatted TableView object.
     * @return a TableView object
     */
    public TableView<Earthquake> newTableView() {

        TableView<Earthquake> table = new TableView<>();

        table.setPrefHeight(180);
        table.setPrefWidth(225);
        TableColumn<Earthquake,Double> magnitudeCol = new TableColumn<>("Magnitude");
        magnitudeCol.setCellValueFactory(new PropertyValueFactory<>("mag"));
        magnitudeCol.setMinWidth(120);
        TableColumn<Earthquake,String> placeCol = new TableColumn<>("Location");
        placeCol.setCellValueFactory(new PropertyValueFactory<>("place"));
        placeCol.setMinWidth(300);
        TableColumn<Earthquake,String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeCol.setMinWidth(115);
        TableColumn<Earthquake,String> detailCol = new TableColumn<>("More Info");
        detailCol.setCellValueFactory(new PropertyValueFactory<>("detail"));
        detailCol.setMinWidth(200);
        TableColumn<Earthquake,Double> latCol = new TableColumn<>("Latitude");
        latCol.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        latCol.setMinWidth(84);
        TableColumn<Earthquake,Double> longCol = new TableColumn<>("Longitude");
        longCol.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        longCol.setMinWidth(84);

        table.getColumns().setAll(magnitudeCol, placeCol, timeCol,
            latCol, longCol, detailCol);
        table.setTableMenuButtonVisible(true);
        selectionModel = table.getSelectionModel();
        return table;
    } // newTableView

    /**
     * Mathod adds objects to this object.
     */
    public void addElements() {

        this.setPadding(new Insets(10, 10, 10, 10));
        this.setAlignment(Pos.TOP_LEFT);
        this.setVgap(5);
        this.setHgap(5);

        this.setPrefHeight(400);
        this.setPrefWidth(330);
        this.add(locate, 0, 0, 2, 1);

        this.add(slider, 0, 2, 2, 1);
        this.add(minMagnitude, 0, 1, 2, 1);
        this.add(selectedMagnitude, 1, 1, 1, 1);

        this.add(beginingDate, 0, 4);
        this.add(datePicker, 1, 4);

        this.add(numOfQuakes, 0, 5);
        this.add(comboBox, 1, 5, 1, 1);

        this.add(regionFilter, 0, 6, 1, 1);
        this.add(regionSelection, 1, 6, 1, 1);

        this.add(results, 0, 8);
        this.add(table, 0, 9, 3, 7);

        this.add(credit, 0, 17, 3, 1);

    } // addElements

    /**
     * Method to format a Stage oibject mainly used to reduce the size of
     * of other methods.
     * @param dialog is the stage to be formated
     * @param dialogScene is a Scene object
     */
    public static void formatStage(Stage dialog, Scene dialogScene) {
        dialog.setTitle("Info");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(dialogScene);
        dialog.show();
    } // formatStage


} // OptionsPane
