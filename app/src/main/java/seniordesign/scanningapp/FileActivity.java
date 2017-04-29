package seniordesign.scanningapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private void copyAssets() {
        String path = Environment.getExternalStorageDirectory().getPath() + MODEL_DIRECTORY;
        String[] files = new File(path).list();
        Arrays.sort(files);
        for(int i=0;i<files.length;i++) {
            String name = files[i];
            if (getModelType(name) != -1) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    String inDir = Environment.getExternalStorageDirectory().getPath() + MODEL_DIRECTORY;
                    File inFile = new File(inDir, name);
                    in = new FileInputStream(inFile);
                    File outDir = new File(getFilesDir(), "00" + i);
                    File outFile = new File(outDir,"model.ply");
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                    in.close();
                    in = null;
                    out.flush();
                    out.close();
                    out = null;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e("tag", "Failed to copy asset file: " + name, e);
                }
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
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
