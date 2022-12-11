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

import java.lang.Thread;

/**
 * Custom component utilized by EarthQuakeApp.
 */
public class OptionsPane extends GridPane {

    Button locate;
    DatePicker datePicker;
    Slider slider;
    Label minMagnitude;
    ComboBox<Integer> comboBox;
    Label selectedMagnitude;
    Label numOfQuakes;
    Label beginingDate;
    ListView list;
    Label results;
    Label resultSource;

    int resultLimit;
    double minimumMagnitude;

    private Runnable runable;
    private Thread resultLoader;

    public OptionsPane() {
        super();
        locate = new Button("Locate");
        datePicker = new DatePicker();
        slider = new Slider(0.1, 9.0, 5);
        minMagnitude = new Label("Minimum Magnitude:");
        comboBox = new ComboBox();
        selectedMagnitude = new Label("5");
        beginingDate = new Label("Begining Date");
        numOfQuakes = new Label("Result Limit");
        list = new ListView();
        list.setPrefHeight(180);
        list.setPrefWidth(225);
        results = new Label("Results:");
        resultSource = new Label("All Earthquake data provided by https://earthquake.usgs.gov");

        runable = () -> loadResults();
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
        this.add(selectedMagnitude, 2, 1);

        this.add(beginingDate, 0, 4);
        this.add(datePicker, 0, 5);

        this.add(numOfQuakes, 0, 6);
        this.add(comboBox, 1, 6);

        this.add(results, 0, 13);
        this.add(list, 0, 14, 3, 1);

        this.add(resultSource, 0, 15);
    } // constructor


    /**
     * Method to get results form earthquake Api.
     */
    private void loadResults() {

        System.out.println("Locating...");

    } // loadResults

} // OptionsPane
