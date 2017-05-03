package seniordesign.scanningapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Elaine on 4/28/2017.
 */

public class WallAdapter extends BaseAdapter{
    private WallActivity mContext;
    private ArrayList<Wall> walls;

    WallAdapter(WallActivity context, ArrayList<Wall> walls) {
        this.mContext = context;
        this.walls = walls;
    }

    @Override
    public int getCount() {
        return walls.size();
    }

    @Override
    public Object getItem(int i) {
        return walls.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.wall_list_item, null, true);
        }
        TextView wallNameView = (TextView) view.findViewById(R.id.wall_name);
        final Wall wall = walls.get(i);
        wallNameView.setText(wall.name);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, RouteActivity.class);
                intent.putExtra(WallActivity.FOLDER_NAME_KEY, wall.modelFileName);
                intent.putExtra(WallActivity.WALL_NAME_KEY, wall.name);
                mContext.startActivity(intent);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final LayoutInflater inflater = mContext.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_info,null);
                TextView textView = (TextView) dialogView.findViewById(R.id.dialog_textbox);
                textView.setText("Are you sure? Will delete all of " + walls.get(i).name + "'s associated routes.");
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Delete Wall")
                        .setCancelable(true)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int index) {
                                boolean success = true;
                                File FILE_LOCATION = mContext.getFilesLocation();
                                File wallsListFile = new File(FILE_LOCATION, WallActivity.WALLS_LIST_NAME);
                                if (wallsListFile.exists()) {
                                    try {
                                        StringBuilder sb = new StringBuilder();
                                        String line;
                                        BufferedReader br = new BufferedReader(new FileReader(wallsListFile));
                                        while ((line = br.readLine()) != null) {
                                            sb.append(line);
                                        }

                                        Log.e("WallAdapter", "From File: " + sb.toString());
                                        JSONObject obj = new JSONObject(sb.toString());
                                        JSONArray wallList = obj.getJSONArray(Wall.WALLS_LIST_JSON_KEY);
                                        wallList.remove(i);
                                        JSONObject newObj = new JSONObject();
                                        newObj.put(Wall.WALLS_LIST_JSON_KEY, wallList);

                                        wallsListFile.delete();
                                        wallsListFile.createNewFile();

                                        FileOutputStream outputStream;
                                        outputStream = new FileOutputStream(wallsListFile);
                                        Log.e("WallAdapter", "Writing: " + newObj.toString());
                                        outputStream.write(newObj.toString().getBytes());
                                        outputStream.close();
                                    } catch (IOException e) {
                                        success = false;
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        success = false;
                                        e.printStackTrace();
                                    }
                                } else {
                                    success = false;
                                }

                                if (success) {
                                    walls.remove(i);
                                    notifyDataSetChanged();
                                }
                            }
                        });
                builder.setView(dialogView);
                builder.create().show();
                return false;
            }
        });
        return view;
    }
}
