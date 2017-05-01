package seniordesign.scanningapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
    public View getView(int i, View view, ViewGroup viewGroup) {
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
        return view;
    }
}
