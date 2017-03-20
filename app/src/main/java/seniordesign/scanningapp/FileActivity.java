package seniordesign.scanningapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Elaine on 3/1/2017.
 */

public class FileActivity extends Activity {
    public static String FILE_NAME_KEY = "fileNameKey";
    protected static final String[] FILE_EXT = {".obj", ".ply"};
    protected static final String MODEL_DIRECTORY = "/Models/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String path = Environment.getExternalStorageDirectory().getPath() + MODEL_DIRECTORY;
        String[] files = new File(path).list();
        Arrays.sort(files);
        ArrayList<String> fileNames = new ArrayList<String>();
        for(String name : files) {
            if (getModelType(name) != -1) {
                fileNames.add(name);
            }
        }
        FileAdapter fileAdapter = new FileAdapter(this, new ArrayList<String>(Arrays.asList(files)));
        findViewById(R.id.no_data).setVisibility(fileAdapter.getCount()==0 ? View.VISIBLE : View.GONE);
        ((ListView)findViewById(R.id.list)).setAdapter(fileAdapter);
    }

    public static int getModelType(String filename) {
        for(int i = 0; i < FILE_EXT.length; i++)
            if(filename.substring(filename.length() - FILE_EXT[i].length()).contains(FILE_EXT[i]))
                return i;
        return -1;
    }
}
