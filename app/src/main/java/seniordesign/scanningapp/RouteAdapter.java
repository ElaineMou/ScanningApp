package seniordesign.scanningapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Elaine on 4/29/2017.
 */

public class RouteAdapter extends BaseAdapter {
    private RouteActivity mContext;
    private ArrayList<Route> routes;
    private String wallPath;
    private File[] fileList;

    RouteAdapter(RouteActivity context, ArrayList<Route> routes, String wallPath, File[] fileList) {
        this.mContext = context;
        this.routes = routes;
        this.wallPath = wallPath;
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.route_list_item, null, true);
        }
        TextView routeNameView = (TextView) view.findViewById(R.id.route_name);
        final Route route = routes.get(i);
        final File file = fileList[i];
        routeNameView.setText(route.getName());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewerActivity.class);

                intent.putExtra(RouteActivity.ROUTE_NAME_KEY, route.getName());
                intent.putExtra(RouteActivity.ROUTE_DIFFICULTY_KEY, route.getDifficulty());
                intent.putExtra(RouteActivity.ROUTE_MARKERS_KEY, route.getMarkersString());
                intent.putExtra(RouteActivity.ROUTE_FILE_KEY, file.getName());
                intent.putExtra(WallActivity.FOLDER_NAME_KEY, wallPath);
                mContext.startActivity(intent);
            }
        });
        return view;
    }
}
