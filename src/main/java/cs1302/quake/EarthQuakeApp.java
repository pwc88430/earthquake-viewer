package cs1302.quake;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;

/**
 * Represents an EarthQuake App.
 */
public class EarthQuakeApp extends Application {

    private Stage stage;
    private Scene scene;
    private HBox root;
    private ImageView imageView;
    private VBox options;
    private OptionsPane optionsPane;
    private StackPane stackPane;
    private ImageView pointer;
    private Image map;

    public EarthQuakeApp() {
        this.stage = null;
        this.scene = null;
        this.root = new HBox();
        this.map = new Image("file:resources/World_location_map_(equirectangular_180).svg-2.png");
        this.imageView = new ImageView(map);
        this.imageView.setFitHeight(400);
        this.imageView.setFitWidth(900);
        this.optionsPane = new OptionsPane(this);
        this.stackPane = new StackPane(imageView);
        System.out.println(imageView.getFitWidth() + " " + imageView.getFitHeight());
    } // EarthQuakeApp

    @Override
    public void init() {

    } // init

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        root.getChildren().addAll(stackPane, optionsPane);
        this.scene = new Scene(this.root);
        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.setTitle("Earth Quake Finder");
        this.stage.setScene(this.scene);
        this.stage.sizeToScene();
        this.stage.show();

        Platform.runLater(() -> this.stage.setResizable(false));

    } // start

    /**
     * Method displays locations of earthquakes.
     * @param earthquakes contains locations of earthquakes
     */
    public void displayPointers(Earthquake[] earthquakes) {

        System.out.println("Printing pointers...");

        ImageView[] pointers = new ImageView[earthquakes.length];

        for (int i = 0; i < earthquakes.length; i++) {


            pointers[i] = new ImageView("file:resources/Location_pointer.png");

            pointers[i].setFitHeight(earthquakes[i].mag*18);
            pointers[i].setFitWidth(earthquakes[i].mag*18);
            pointers[i].setTranslateX(earthquakes[i].longitude * 2.5);
            pointers[i].setTranslateY(earthquakes[i].latitude * -2.2);
        } // for

        Platform.runLater(() -> stackPane.getChildren().clear());
        Platform.runLater(() -> stackPane.getChildren().add(imageView));
        Platform.runLater(() -> stackPane.getChildren().addAll(pointers));


    } // displayPointers




} // EarthQuakeApp
