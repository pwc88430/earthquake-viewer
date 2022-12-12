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
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.lang.Thread;

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

    int resultLimit;
    double minimumMagnitude;
    LocalDate defaultDate;
    TableViewSelectionModel<Earthquake> selectionModel;

    private Runnable runable, moreInfo;
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

    private static Image zoomedImage = new Image("file:resources/World_location_map_(equirectangular_180).svg-2.png");

    private static final String EARTHQUAKE_API = "https://earthquake.usgs.gov/fdsnws/event/1";

    private static final String FORMAT = "format=geojson";

    private static final String METHOD = "query";

    String[] amounts = {"1", "5", "10", "15", "20"};
    String[] regions = {"Northern hemisphere", "Southern hemisphere",
        "Eastern hemisphere", "Western hemisphere", "All regions"};

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

        regionFilter = new Label("Filter by region:");
        regionSelection = new ComboBox<>(FXCollections.observableArrayList(regions));
        regionSelection.setValue("All regions");
        minMagnitude = new Label("Minimum Magnitude:  ");
        comboBox = new ComboBox<>(FXCollections.observableArrayList(amounts));
        comboBox.setValue("5");
        comboBox.setPrefWidth(55);

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
        timeCol.setMinWidth(115);
        TableColumn<Earthquake,String> detailCol = new TableColumn<>("More Info");
        detailCol.setCellValueFactory(new PropertyValueFactory<>("detail"));
        detailCol.setMinWidth(200);
        //TableColumn<Earthquake,String> typeCol = new TableColumn<>("Type");
        //typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        //typeCol.setMinWidth(84);
        TableColumn<Earthquake,Double> latCol = new TableColumn<>("Latitude");
        latCol.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        latCol.setMinWidth(84);
        TableColumn<Earthquake,Double> longCol = new TableColumn<>("Longitude");
        longCol.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        longCol.setMinWidth(84);


        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(43);
        this.getColumnConstraints().addAll(col1);


        table.getColumns().setAll(magnitudeCol, placeCol, timeCol,
        latCol, longCol, detailCol);
        table.setTableMenuButtonVisible(true);
        selectionModel = table.getSelectionModel();

        results = new Label("Results:");
        resultSource = new Label("All Earthquake data provided by https://earthquake.usgs.gov");

        moreInfo = () -> getMoreInfo(new Earthquake(3.1, "b", 3422, "hgsdfu", "hsgd", 234.2, 143));


        runable = () -> loadResults(Integer.parseInt(comboBox.getValue()),
        Double.parseDouble(selectedMagnitude.getText()), date, regionSelection.getValue(),
        datePicker.getValue().toString());
        resultLoader = new Thread(runable);
        resultLoader.setDaemon(true);
        locate.setOnAction(event -> new Thread(resultLoader).start());

        this.setPadding(new Insets(10, 10, 10, 10));
        this.setAlignment(Pos.TOP_LEFT);
        this.setVgap(5);
        this.setHgap(5);

        this.setPrefHeight(400);
        this.setPrefWidth(330);
        this.add(locate, 0, 0, 2, 1);

        this.add(slider, 0, 2, 2, 1);
        this.add(minMagnitude, 0, 1, 2, 1);
        this.add(selectedMagnitude, 1, 1, 1
        , 1);

        this.add(beginingDate, 0, 4);
        this.add(datePicker, 1, 4);

        this.add(numOfQuakes, 0, 5);
        this.add(comboBox, 1, 5, 1, 1);

        this.add(regionFilter, 0, 6, 1, 1);
        this.add(regionSelection, 1, 6, 1, 1);

        this.add(results, 0, 8);
        this.add(table, 0, 9, 3, 7);

        //this.add(resultSource, 0, 15);
    } // constructor


    /**
     * Method to get results form earthquake Api.
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

            earthquakes[i] = new Earthquake(mag, place, time, detail, type, longitude, latitude);
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


        } catch(Exception e) {

            System.out.println(e);

        } // try-catch


    } // loadResults


        /**
     * BuildRequest builds an HTTP request object.
     */
    public HttpRequest buildRequest(int resultLimit, double minMagnitude,
    float Date, String region, String starttime) {

        System.out.println(datePicker.getValue());

        if (starttime == null) {
            starttime = "";
        } else {
            starttime = "&starttime=" + starttime;
        }


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
     */
    public static void getMoreInfo(Earthquake earthquake) {

        System.out.println("Getting more info...");

        Label title = new Label("Type: Earthquake");

        Label location = new Label("Location: " + earthquake.place);

        Label mag = new Label("Magnitude: " + earthquake.mag);

        Label time = new Label("Time: " + earthquake.time);

        Label longitude = new Label("Longitude: " + earthquake.longitude);

        Label latitude = new Label("Latitude: " + earthquake.latitude);

        Button back = new Button("Back to map");

        ImageView zoom = new ImageView();

        Label moreInfo = new Label("More info: ");

        Label info = new Label("Lots of interesting information...");

        zoom.setViewport(new Rectangle2D(470 + earthquake.longitude * 2.5, 210 + earthquake.latitude * -2.2, 330, 220));

        //zoom.setFitWidth(330);
        //zoom.setFitHeight(175);
        zoom.setImage(zoomedImage);


        final Stage dialog = new Stage();
        dialog.setTitle("Info");
        dialog.initModality(Modality.APPLICATION_MODAL);
        //dialog.initOwner(primaryStage);
        GridPane grid = new GridPane();
        Scene dialogScene = new Scene(grid, 350, 600);
        dialog.setScene(dialogScene);
        dialog.show();

        Runnable closeInfo = () -> dialog.close();

        back.setOnAction(event -> closeInfo.run());

        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setVgap(5);
        grid.setHgap(5);
        grid.add(zoom, 0, 0);
        grid.add(title, 0, 1);
        grid.add(location, 0, 2);
        grid.add(mag, 0, 3);
        grid.add(time, 0, 4);
        grid.add(longitude, 0, 5);
        grid.add(latitude, 0, 6);
        grid.add(moreInfo, 0, 7);
        grid.add(info, 0, 8);

        grid.add(back, 0, 11);

    } // getMoreInfo


} // OptionsPane
