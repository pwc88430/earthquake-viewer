package cs1302.quake;

import javafx.application.Application;

/**
 * Driver class for EarthQuakeApp.
 */
public class EarthQuakeDriver {


    public static void main(String[] args) {

        try {
            Application.launch(EarthQuakeApp.class, args);
        } catch (UnsupportedOperationException e) {
            System.out.println(e);

        } // try-catch





    } // main

} // EarthQuakeDriver
