package seniordesign.scanningapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public JSONArray getMarkersAsJson() throws JSONException {
        JSONArray array = new JSONArray();
        for(MarkerInfo info : markers) {
            array.put(info.toJSON());
        }
        return array;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NAME_JSON_KEY,name);
        jsonObject.put(DIFFICULTY_JSON_KEY,difficulty);
        jsonObject.put(DESCRIPTION_JSON_KEY,description);
        jsonObject.put(MARKERS_JSON_KEY,getMarkersAsJson());
        return jsonObject;
    }

    public void setMarkersFromJson(String markersJson) throws JSONException {
        ArrayList<MarkerInfo> list = MarkerInfo.MarkersFromJson(markersJson);
        setMarkers(list);
    }
}
