package seniordesign.scanningapp;

import java.util.ArrayList;

/**
 * Created by Elaine on 4/28/2017.
 */

public class Route {
    public static final String NAME_JSON_KEY = "name";
    public static final String DIFFICULTY_JSON_KEY = "difficulty";
    public static final String DESCRIPTION_JSON_KEY = "description";
    public static final String MARKERS_JSON_KEY = "markers";

    private String name;
    private String difficulty;
    private String description;
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
    public String getMarkersString() {return "";}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
