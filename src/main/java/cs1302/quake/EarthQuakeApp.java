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


/**
 * Represents an EarthQuake App.
 */
public class EarthQuakeApp extends Application {

    private Stage stage;
    private Scene scene;
    private HBox root;
    private ImageView imageView;
    private VBox options;


    public EarthQuakeApp() {
        this.stage = null;
        this.scene = null;
        this.root = new HBox();
        this.imageView = new ImageView(new Image("file:resources/World_outline_map.png"));
        this.imageView.setFitHeight(400);
        this.imageView.setFitWidth(900);
        this.options = new VBox();
        this.options.setPrefHeight(400);
        this.options.setPrefWidth(250);
        this.options.setBackground(new Background(new BackgroundFill(Color.BEIGE, null, null)));
    } // EarthQuakeApp

    @Override
    public void init() {

    } // init

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        root.getChildren().addAll(imageView, options);
        this.scene = new Scene(this.root);
        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.setTitle("Earth Quake Finder");
        this.stage.setScene(this.scene);
        this.stage.sizeToScene();
        this.stage.show();

        Platform.runLater(() -> this.stage.setResizable(false));

    } // start






} // EarthQuakeApp
