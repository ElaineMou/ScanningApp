package seniordesign.scanningapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Elaine on 4/28/2017.
 */

public class RouteActivity extends Activity {
    public static final String ROUTE_NAME_KEY = "routeNameKey";
    public static final String ROUTE_DIFFICULTY_KEY = "routeDiffKey";
    public static final String ROUTE_MARKERS_KEY = "routeMarksKey";
    public static final String ROUTE_FILE_KEY = "routeFileKey";

    private FloatingActionButton fab;
    private File FILE_LOCATION;
    private ArrayList<Route> routes = new ArrayList<>();
    private String wallName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wallName = getIntent().getStringExtra(WallActivity.WALL_NAME_KEY);
        String wallDir = getIntent().getStringExtra(WallActivity.FOLDER_NAME_KEY);
        FILE_LOCATION = new File(getFilesDir(), wallDir);
        setContentView(R.layout.activity_routes);
    }

    @Override
    protected void onResume() {
        super.onResume();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setRippleColor(ContextCompat.getColor(this,R.color.colorPrimary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RouteActivity.this, ViewerActivity.class);
                intent.putExtra(WallActivity.FOLDER_NAME_KEY, FILE_LOCATION.getName());
                startActivity(intent);
            }
        });

        ((TextView) findViewById(R.id.title)).setText(wallName);

        File[] routeFiles = FILE_LOCATION.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(ViewerActivity.ROUTE_FILE_SUFFIX);
            }
        });
        ArrayList<File> filesArrayList = new ArrayList<>();
        routes.clear();
        if(routeFiles!=null && routeFiles.length>0) {
            try {
                filesArrayList = new ArrayList<File>(Arrays.asList(routeFiles));
                for(int i=0;i<filesArrayList.size();i++) {

                    FileReader reader = new FileReader(filesArrayList.get(i));
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    StringBuffer sb = new StringBuffer();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    bufferedReader.close();
                    JSONObject jsonObject = new JSONObject(sb.toString());
                    String routeName = jsonObject.getString(Route.NAME_JSON_KEY);
                    String difficulty = jsonObject.getString(Route.DIFFICULTY_JSON_KEY);
                    String description = jsonObject.getString(Route.DESCRIPTION_JSON_KEY);
                    String markersJson = jsonObject.getString(Route.MARKERS_JSON_KEY);

                    Route route = new Route(routeName,difficulty);
                    route.setDescription(description);
                    route.setMarkersFromJson(markersJson);
                    routes.add(route);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        RouteAdapter routeAdapter = new RouteAdapter(this,routes,FILE_LOCATION.getName(),filesArrayList);
        findViewById(R.id.no_data).setVisibility(routeAdapter.getCount()==0 ? View.VISIBLE : View.GONE);
        ((ListView)findViewById(R.id.list)).setAdapter(routeAdapter);
    }
}
