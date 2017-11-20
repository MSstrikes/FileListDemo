package com.xiang.david.filelistdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiang.david.filelistdemo.R;
import com.xiang.david.filelistdemo.model.DirListItem;
import com.xiang.david.filelistdemo.model.FileListItem;
import com.xiang.david.filelistdemo.model.OriginItem;

import java.util.ArrayList;

/**
 * Created by msstrike on 2017/11/18.
 */

public class ListAdapter extends BaseAdapter {
    private ArrayList<OriginItem> listItems = null;
    private Context context = null;
    public ListAdapter(ArrayList<FileListItem> listItems, Context context){
        this.listItems = listItems;
        this.context = context;
    }
    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        if (fileListItems == null && dirListItems == null) return null;
        if (fileListItems == null) return dirListItems.get(position);
        if (dirListItems == null) return fileListItems.get(position);
        if (position < dirListItems.size()){
            return dirListItems.get(position);
        } else {
            return fileListItems.get(position - dirListItems.size());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView =  inflater.inflate(R.layout.main_list_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        return null;
    }

    public class ViewHolder{
        public ImageView icon;
        public TextView name;
        public TextView attribute1;
        public TextView attribute2;

        public ViewHolder(View convertView){
            icon = (ImageView) convertView.findViewById(R.id.item_icon);
            name = (TextView) convertView.findViewById(R.id.item_name);
            attribute1 = (TextView) convertView.findViewById(R.id.item_attribute1);
            attribute2 = (TextView) convertView.findViewById(R.id.item_attribute2);
        }

    }
}
