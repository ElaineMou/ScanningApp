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
 * Created by Elaine on 3/2/2017.
 */

public class FileAdapter extends BaseAdapter {

    private FileActivity mContext;
    private ArrayList<String> fileNames;

    FileAdapter(FileActivity context, ArrayList<String> names) {
        mContext = context;
        fileNames = names;
    }

    @Override
    public int getCount() {
        return fileNames.size();
    }

    @Override
    public Object getItem(int i) {
        return fileNames.get(i);
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
            view = inflater.inflate(R.layout.file_list_item, null, true);
        }
        TextView fileNameView = (TextView) view.findViewById(R.id.file_name);
        fileNameView.setText((String)getItem(i));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, AugmentedRealityActivity.class);
                intent.putExtra(FileActivity.FILE_NAME_KEY, fileNames.get(i));
                mContext.startActivity(intent);
            }
        });

        return view;
    }
}
