package cs1302.quake;

import javafx.stage.PopupWindow;

/**
 * A class to provide more info about a specific earthquake.
 */
public class InfoWindow extends PopupWindow {

    Double mag;
    String type;

    public InfoWindow(Earthquake earthquake) {
        super();
        this.mag = earthquake.mag;
        this.type = earthquake.type;
    } // constructor

} // InfoWindow
