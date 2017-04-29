package seniordesign.scanningapp;

/**
 * Created by Elaine on 4/28/2017.
 */

public class Wall {
    public final String name;
    public final String modelFileName;
    public final double[] geo;
    public Wall(String name, String modelFileName, double[] geo) {
        this.name = name;
        this.modelFileName = modelFileName;
        this.geo = geo;
    }
}
