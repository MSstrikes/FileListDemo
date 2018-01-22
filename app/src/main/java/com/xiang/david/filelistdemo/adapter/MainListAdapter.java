package com.xiang.david.filelistdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.xiang.david.filelistdemo.R;
import com.xiang.david.filelistdemo.model.DirListItem;
import com.xiang.david.filelistdemo.model.FileListItem;
import com.xiang.david.filelistdemo.model.OriginItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by msstrike on 2017/11/18.
 */

public class MainListAdapter extends BaseAdapter {
    private ArrayList<OriginItem> listItems = null;
    private Context context = null;
    private Map<Integer, Boolean> transferingItem = new HashMap<>();
    public MainListAdapter(ArrayList<OriginItem> listItems, Context context){
        this.listItems = listItems;
        this.context = context;
    }

    public void setItemState(int position, boolean state){
        transferingItem.put(position, state);
    }
    @Override
    public int getCount() {
        if (listItems != null)  return listItems.size();
        return 0;
    }

    @Override
    public OriginItem getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OriginItem item = listItems.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView =  inflater.inflate(R.layout.main_list_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (transferingItem.get(position) != null && transferingItem.get(position) == true){
            viewHolder.progressBarLayout.setVisibility(View.VISIBLE);
            viewHolder.transferTitle.setVisibility(View.VISIBLE);
        } else {
            viewHolder.progressBarLayout.setVisibility(View.GONE);
            viewHolder.transferTitle.setVisibility(View.GONE);
        }
        viewHolder.icon.setImageDrawable(context.getResources().getDrawable(item.getIcon()));
        viewHolder.name.setText(item.getName());
        switch (item.getType()){
            case DIR:{
                DirListItem dirItem = (DirListItem) item;
                viewHolder.attribute1.setText("文件夹: " + dirItem.getDirCounts());
                viewHolder.attribute2.setText("文件: " + dirItem.getFileCounts());
                dirItem = null;
                break;
            }
            case FILE:{
                FileListItem fileItem = (FileListItem) item;
                viewHolder.attribute1.setText("类型: " + fileItem.getFileType());
                viewHolder.attribute2.setText("大小: " + fileItem.getFileSize());
                fileItem = null;
                break;
            }
            default:
                break;
        }

        return convertView;
    }

    public class ViewHolder{
        public ImageView icon;
        public TextView name;
        public TextView attribute1;
        public TextView attribute2;
        public TextView transferTitle;
        public NumberProgressBar progressBar;
        public LinearLayout progressBarLayout;

        public ViewHolder(View convertView){
            icon = (ImageView) convertView.findViewById(R.id.item_icon);
            name = (TextView) convertView.findViewById(R.id.item_name);
            attribute1 = (TextView) convertView.findViewById(R.id.item_attribute1);
            attribute2 = (TextView) convertView.findViewById(R.id.item_attribute2);
            progressBar = (NumberProgressBar) convertView.findViewById(R.id.item_progress_bar);
            transferTitle = (TextView) convertView.findViewById(R.id.item_transport_title);
            progressBarLayout = (LinearLayout) convertView.findViewById(R.id.item_progress_layout);
        }

    }
}
