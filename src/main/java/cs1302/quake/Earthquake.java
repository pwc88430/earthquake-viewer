package cs1302.quake;

import java.time.*;
import java.time.format.DateTimeFormatter;
/**
 * Represents an Earthquake object.
 */
public class Earthquake {

    double mag;
    String place;
    String time;
    String detail;
    String type;
    double longitude;
    double latitude;

    public Earthquake(double mag, String place, long time, String detail, String type,
    double longitude, double latitude) {

         ZonedDateTime dateTime = Instant.ofEpochMilli(time)
            .atZone(ZoneId.of("Australia/Sydney"));

         String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        this.mag = mag;
        this.place = place;
        this.time = formatted;
        this.detail = detail;
        this.type = type;
        this.longitude = longitude;
        this.latitude = latitude;
    } // constructor

    /**
     * Get mag.
     */
    public double getMag() {
        return mag;
    } // getMag

    /**
     * Get place.
     */
    public String getPlace() {
        return place;
    } // getPlace

    /**
     * Get time.
     */
    public String  getTime() {
        return time;
    } // getTime

    /**
     * Get detail.
     */
    public String getDetail() {
        return detail;
    } // getDetail

    /**
     * Get type.
     */
    public String getType() {
        return type;
    } // getType

    /**
     * Get latitude.
     */
    public double getLatitude() {
        return latitude;
    } //getLatitude

    /**
     * Get longitude.
     */
    public double getLongitude() {
        return longitude;
    } // getLongitude
} // Earthquake
