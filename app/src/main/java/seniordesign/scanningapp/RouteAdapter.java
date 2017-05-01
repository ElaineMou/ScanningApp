package seniordesign.scanningapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Elaine on 4/29/2017.
 */

public class RouteAdapter extends BaseAdapter {
    private RouteActivity mContext;
    private ArrayList<Route> routes;
    private String wallFolderName;
    private ArrayList<File> fileList;

    RouteAdapter(RouteActivity context, ArrayList<Route> routes, String wallFolderName, ArrayList<File> fileList) {
        this.mContext = context;
        this.routes = routes;
        this.wallFolderName = wallFolderName;
        this.fileList = fileList;
    }

    @Override
    public int getCount() {
        return routes.size();
    }

    @Override
    public Object getItem(int i) {
        return routes.get(i);
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
            view = inflater.inflate(R.layout.route_list_item, null, true);
        }
        TextView routeNameView = (TextView) view.findViewById(R.id.route_name);
        final Route route = routes.get(i);
        routeNameView.setText(route.getName());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewerActivity.class);
                intent.putExtra(WallActivity.FOLDER_NAME_KEY, wallFolderName);
                intent.putExtra(RouteActivity.ROUTE_NAME_KEY, route.getName());
                intent.putExtra(RouteActivity.ROUTE_DIFFICULTY_KEY, route.getDifficulty());
                try {
                    intent.putExtra(RouteActivity.ROUTE_MARKERS_KEY,
                                    route.getMarkersAsJson().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                intent.putExtra(RouteActivity.ROUTE_FILE_KEY, fileList.get(i).getName());
                mContext.startActivity(intent);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                String routeName = route.getName();
                builder.setTitle("Delete " + routeName + "?")
                        .setCancelable(true)
                        .setNegativeButton("No",null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int j) {
                                File file = fileList.get(i);
                                file.delete();
                                fileList.remove(i);
                                routes.remove(i);
                                notifyDataSetChanged();
                            }
                        });
                builder.create().show();
                return false;
            }
        });
        return view;
    }
}
