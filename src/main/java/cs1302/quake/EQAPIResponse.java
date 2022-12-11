package cs1302.quake;

import cs1302.quake.Metadata;
import cs1302.quake.Feature;

/**
 * EQAPIResponse is an object for storing parsed json objects
 * as a result of Http requests.  These objects are created using the GSON
 * library;
 */
public class EQAPIResponse {
    String type;
    Metadata metadata;
    Feature[] features;
} // EQAPIResponse
