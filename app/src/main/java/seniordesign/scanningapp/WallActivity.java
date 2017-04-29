package seniordesign.scanningapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static seniordesign.scanningapp.FileActivity.FILE_EXT;
import static seniordesign.scanningapp.FileActivity.FILE_NAME_KEY;

/**
 * Created by Elaine on 4/28/2017.
 */

public class WallActivity extends Activity {
    public static String FOLDER_NAME_KEY = "wallFileNameKey";
    public static final String WALLS_LIST_NAME = "walls.json";
    public static final String WALL_MODEL_NAME = "model.ply";
    public static final String WALLS_LIST_JSON_KEY = "wallsList";
    public static final String WALL_NAME_JSON_KEY = "wallName";
    public static final String WALL_FOLDER_JSON_KEY = "wallFolder";
    public static final String WALL_LAT_JSON_KEY = "wallLat";
    public static final String WALL_LONG_JSON_KEY = "wallLong";
    private File FILE_LOCATION;
    private ArrayList<Wall> walls = new ArrayList<>();
    String string = "{\n" +
            " \"wallsList\":[\n" +
            "   {\n" +
            "     \"wallName\": \"Boulder Wall\",\n" +
            "     \"wallFolder\": \"000\",\n" +
            "     \"wallLat\": 41.6571,\n" +
            "     \"wallLong\": 91.5385\n" +
            "   },\n" +
            "   {\n" +
            "     \"wallName\": \"Bubble Chair\",\n" +
            "     \"wallFolder\": \"001\",\n" +
            "     \"wallLat\": 40.4237,\n" +
            "     \"wallLong\": 86.9212\n" +
            "   },\n" +
            "   {\n" +
            "     \"wallName\": \"Lego Wall\",\n" +
            "     \"wallFolder\": \"002\",\n" +
            "     \"wallLat\": 40.1020,\n" +
            "     \"wallLong\": 88.2272\n" +
            "   },\n" +
            "   {\n" +
            "     \"wallName\": \"Lower Boulder Wall\",\n" +
            "     \"wallFolder\": \"003\",\n" +
            "     \"wallLat\": 41.6571,\n" +
            "     \"wallLong\": 91.5385\n" +
            "   },\n" +
            "   {\n" +
            "     \"wallName\": \"Rotten Banana\",\n" +
            "     \"wallFolder\": \"004\",\n" +
            "     \"wallLat\": 41.6571,\n" +
            "     \"wallLong\": 91.5385\n" +
            "   },\n" +
            "   {\n" +
            "     \"wallName\": \"Yellow Route\",\n" +
            "     \"wallFolder\": \"005\",\n" +
            "     \"wallLat\": 41.6571,\n" +
            "     \"wallLong\": 91.5385\n" +
            "   },\n" +
            " ]\n" +
            "}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FILE_LOCATION = getFilesDir();
        /*File file = new File(FILE_LOCATION, WALLS_LIST_NAME);
        if(file.exists()) {
            file.delete();
        }

        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(WALLS_LIST_NAME, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        setContentView(R.layout.activity_walls);
    }

    @Override
    protected void onResume() {
        super.onResume();
        File file = new File(FILE_LOCATION,WALLS_LIST_NAME);
        if(file.exists()) {
            walls.clear();
            try {
                FileReader reader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuffer sb = new StringBuffer();
                String line;
                while((line = bufferedReader.readLine())!=null) {
                    sb.append(line);
                }
                bufferedReader.close();
                JSONObject jsonObject = new JSONObject(sb.toString());
                JSONArray wallsList = jsonObject.getJSONArray(WALLS_LIST_JSON_KEY);
                for(int i=0;i<wallsList.length();i++) {
                    JSONObject wall = wallsList.getJSONObject(i);
                    String wallName = wall.getString(WALL_NAME_JSON_KEY);
                    String wallFolder = wall.getString(WALL_FOLDER_JSON_KEY);
                    double wallLat = wall.getDouble(WALL_LAT_JSON_KEY);
                    double wallLong = wall.getDouble(WALL_LONG_JSON_KEY);
                    File folderFile = new File(FILE_LOCATION, wallFolder);
                    if (folderFile.exists()) {
                        walls.add(new Wall(wallName, wallFolder, new double[]{wallLat, wallLat}));
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        WallAdapter wallAdapter = new WallAdapter(this,walls);
        findViewById(R.id.no_data).setVisibility(wallAdapter.getCount()==0 ? View.VISIBLE : View.GONE);
        ((ListView)findViewById(R.id.list)).setAdapter(wallAdapter);
    }

    public static int getModelType(String filename) {
        for(int i = 0; i < FILE_EXT.length; i++)
            if(filename.substring(filename.length() - FILE_EXT[i].length()).contains(FILE_EXT[i]))
                return i;
        return -1;
    }
}
