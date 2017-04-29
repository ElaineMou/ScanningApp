package seniordesign.scanningapp;

import java.util.ArrayList;

/**
 * Created by Elaine on 4/28/2017.
 */

public class Route {
    private String name;
    private String difficulty;
    private ArrayList<MarkerInfo> markers;

    public Route(String name, String difficulty) {
        this.name = name;
        this.difficulty = difficulty;
        this.markers = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDifficulty() {
        return difficulty;
    }
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    public ArrayList<MarkerInfo> getMarkers() {
        return markers;
    }
    public void setMarkers(ArrayList<MarkerInfo> markers) {
        this.markers = markers;
    }

}
