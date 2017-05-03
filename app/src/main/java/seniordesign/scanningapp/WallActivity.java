package seniordesign.scanningapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import static seniordesign.scanningapp.FileActivity.FILE_EXT;
import static seniordesign.scanningapp.Wall.WALLS_LIST_JSON_KEY;

/**
 * Created by Elaine on 4/28/2017.
 */

public class WallActivity extends Activity {
    public static final String WALL_FOLDER_PREFIX = "wall";
    public static final String MODEL_DIRECTORY = "/Models/";
    public static final String WALLS_LIST_NAME = "walls.json";
    public static final String WALL_MODEL_NAME = "model.ply";
    private File FILE_LOCATION;

    // to be sent on to route activity
    public static String FOLDER_NAME_KEY = "wallFileNameKey";
    public static String WALL_NAME_KEY = "wallNameKey";

    private FloatingActionButton fab;
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
        /*File[] files = FILE_LOCATION.listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                File[] subFiles = file.listFiles();
                for(File subFile : subFiles) {
                    subFile.delete();
                }
            }
            file.delete();
        }*/

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
        File file = new File(FILE_LOCATION, WALLS_LIST_NAME);
        if (file.exists()) {
            walls.clear();
            try {
                FileReader reader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();
                JSONObject jsonObject = new JSONObject(sb.toString());
                Log.e("WallActivity",sb.toString());
                JSONArray wallsList = jsonObject.getJSONArray(WALLS_LIST_JSON_KEY);
                for (int i = 0; i < wallsList.length(); i++) {
                    JSONObject wall = wallsList.getJSONObject(i);
                    String wallName = wall.getString(Wall.NAME_JSON_KEY);
                    String wallFolder = wall.getString(Wall.FOLDER_JSON_KEY);
                    double wallLat = wall.getDouble(Wall.LAT_JSON_KEY);
                    double wallLong = wall.getDouble(Wall.LONG_JSON_KEY);
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
        final WallAdapter wallAdapter = new WallAdapter(this, walls);
        findViewById(R.id.no_data).setVisibility(wallAdapter.getCount() == 0 ? View.VISIBLE : View.GONE);
        ((ListView) findViewById(R.id.list)).setAdapter(wallAdapter);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setRippleColor(ContextCompat.getColor(this,R.color.colorPrimary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] modelsToImport = new File(getSourcePath()).list();

                final LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_new_wall,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(WallActivity.this);
                builder.setTitle("Import Wall")
                        .setCancelable(true)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }
                        );
                builder.setView(dialogView);
                final Spinner spinner = (Spinner) dialogView.findViewById(R.id.dialog_spinner);
                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>
                        (WallActivity.this,android.R.layout.simple_spinner_item,modelsToImport);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                final EditText nameText = (EditText) dialogView.findViewById(R.id.dialog_textbox);

                final AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = nameText.getText().toString().trim();
                        boolean done = !name.isEmpty();
                        // if EditText is empty disable closing on possitive button
                        if (done) {
                            boolean success = false;
                            String modelToImport = spinner.getSelectedItem().toString();
                            String folderName = copyModelToInternal(modelToImport);
                            if (folderName!=null) {
                                success = true;
                                File wallsListFile = new File(FILE_LOCATION, WALLS_LIST_NAME);
                                try {
                                    JSONArray wallList;
                                    if(wallsListFile.exists()) {
                                        StringBuilder sb = new StringBuilder();
                                        String line;
                                        BufferedReader br = new BufferedReader(new FileReader(wallsListFile));
                                        while ((line = br.readLine()) != null) {
                                            sb.append(line);
                                        }

                                        Log.e("WallActivity", "From File: " + sb.toString());
                                        JSONObject obj = new JSONObject(sb.toString());
                                        wallList = obj.getJSONArray(Wall.WALLS_LIST_JSON_KEY);
                                    } else {
                                        wallList = new JSONArray();
                                    }
                                    JSONObject newWall = new JSONObject();
                                    newWall.put(Wall.NAME_JSON_KEY,name);
                                    newWall.put(Wall.FOLDER_JSON_KEY,folderName);
                                    newWall.put(Wall.LAT_JSON_KEY,0.0f);
                                    newWall.put(Wall.LONG_JSON_KEY,0.0f);
                                    wallList.put(newWall);
                                    JSONObject newObj = new JSONObject();
                                    newObj.put(Wall.WALLS_LIST_JSON_KEY, wallList);

                                    wallsListFile.delete();
                                    wallsListFile.createNewFile();

                                    FileOutputStream outputStream;
                                    outputStream = new FileOutputStream(wallsListFile);
                                    Log.e("WallActivity", "Writing: " + newObj.toString());
                                    outputStream.write(newObj.toString().getBytes());
                                    outputStream.close();
                                } catch (IOException e) {
                                    success = false;
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    success = false;
                                    e.printStackTrace();
                                }
                            }

                            if(!success) {
                                Toast.makeText(WallActivity.this, "Error saving to file", Toast.LENGTH_SHORT);
                            } else {
                                Toast.makeText(WallActivity.this, "Wall " + name + " saved.",
                                        Toast.LENGTH_SHORT);
                                walls.add(new Wall(name,folderName,new double[]{0.0f,0.0f}));
                                wallAdapter.notifyDataSetChanged();
                            }
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    public static int getModelType(String filename) {
        for (int i = 0; i < FILE_EXT.length; i++)
            if (filename.substring(filename.length() - FILE_EXT[i].length()).contains(FILE_EXT[i]))
                return i;
        return -1;
    }

    public static String getSourcePath() {
        String dir = Environment.getExternalStorageDirectory().getPath() + MODEL_DIRECTORY;
        return dir;
    }

    public String copyModelToInternal(String modelName) {
        boolean success = true;

        InputStream in = null;
        OutputStream out = null;
        File outFolder = null;

        try {
            String inDir = getSourcePath();
            File inFile = new File(inDir, modelName);
            in = new FileInputStream(inFile);

            outFolder = uniqueFileName(FILE_LOCATION);
            outFolder.mkdir();
            File outFile = new File(outFolder, WALL_MODEL_NAME);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (IOException e) {
            Log.e("tag", "Failed to copy asset file: " + modelName, e);
            return null;
        }

        return outFolder.getName();
    }

    private File uniqueFileName(File directory) {
        int i = 0;
        File file = null;
        while (file == null || file.exists()) {
            String name = WALL_FOLDER_PREFIX + String.format("%03d", i);
            file = new File(directory, name);
            i++;
        }

        return file;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

    }

    public File getFilesLocation() {
        return FILE_LOCATION;
    }
}
