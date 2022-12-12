package cs1302.quake;

/**
 * Represents an Earthquake object.
 */
public class Earthquake {

    double mag;
    String place;
    long time;
    String detail;
    String type;
    double longitude;
    double latitude;

    public Earthquake(double mag, String place, long time, String detail, String type,
    double longitude, double latitude) {
        this.mag = mag;
        this.place = place;
        this.time = time;
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
    public long  getTime() {
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
} // Earthquake
