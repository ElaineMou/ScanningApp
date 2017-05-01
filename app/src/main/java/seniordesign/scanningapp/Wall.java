package seniordesign.scanningapp;

/**
 * Created by Elaine on 4/28/2017.
 */

public class Wall {
    public static final String WALLS_LIST_JSON_KEY = "wallsList";
    public static final String NAME_JSON_KEY = "wallName";
    public static final String FOLDER_JSON_KEY = "wallFolder";
    public static final String LAT_JSON_KEY = "wallLat";
    public static final String LONG_JSON_KEY = "wallLong";

    public final String name;
    public final String modelFileName;
    public final double[] geo;
    public Wall(String name, String modelFileName, double[] geo) {
        this.name = name;
        this.modelFileName = modelFileName;
        this.geo = geo;
    }
}
