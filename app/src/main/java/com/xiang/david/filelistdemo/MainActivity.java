package com.xiang.david.filelistdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.xiang.david.filelistdemo.adapter.MainListAdapter;
import com.xiang.david.filelistdemo.factroy.DirItemFactory;
import com.xiang.david.filelistdemo.factroy.FileIntentFactory;
import com.xiang.david.filelistdemo.factroy.FileItemFactory;
import com.xiang.david.filelistdemo.model.DirListItem;
import com.xiang.david.filelistdemo.model.FileListItem;
import com.xiang.david.filelistdemo.model.OriginItem;
import com.xiang.david.filelistdemo.view.LongClickDialog;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/*
    注意 getAbsolutePath获得的地址最后没有以‘/’结束，必须自己添加
 */
public class MainActivity extends Activity {
    String[] listFiles = null;
    ListView mainList = null;
    TextView backBtn = null;
    TextView currentPathText = null;

    String originPath = "/mnt/sdcard";
    String currentPath = null;
    int currentPoistion = 0;
    boolean backFlag = false;
    int[] clickOrder = new int[100];

    DirItemFactory dirFactory = new DirItemFactory();
    FileItemFactory fileFactory = new FileItemFactory();
    FileIntentFactory fileIntentFactory = new FileIntentFactory();

    ArrayList<OriginItem> fileListItems = new ArrayList<>();
    ArrayList<OriginItem> dirListItems = new ArrayList<>();
    ArrayList<OriginItem> mainListItems = new ArrayList<>();

    static MainListAdapter mainAdapter = null;

    LongClickDialog longClickDialog = null;

    FileFilter dirFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            if (pathname.getName().indexOf(".") == 0) return false;
            if (pathname.isDirectory()) return true;
            return false;
        }
    };

    FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            if (pathname.getName().indexOf(".") == 0) return false;
            if (pathname.isFile()) return true;
            return false;
        }
    };
    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (longClickDialog != null){
                mainHandler.setItemView(view, position);
                longClickDialog.show(getFragmentManager(), "LongClickDialog");
                Bundle fileBundle = new Bundle();
                OriginItem item = mainListItems.get(position);
                fileBundle.putString("filePath",item.getAbsolutePath() + "/" + item.getName());
                longClickDialog.setArguments(fileBundle);
                longClickDialog.setCancelable(true);
            }
            return true;
        }
    };

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            OriginItem originItem = mainAdapter.getItem(position);
            switch (originItem.getType()){
                case FILE:{
                    FileListItem item = (FileListItem) originItem;
                    Intent intent = fileIntentFactory.generateFileIntent(item);
                    startActivity(intent);
                    break;
                }
                case DIR:{
                    clickOrder[currentPoistion] = position;
                    currentPoistion++;
                    StringBuilder parentPathBuilder = new StringBuilder(originItem.getAbsolutePath());
                    parentPathBuilder.append("/");
                    parentPathBuilder.append(originItem.getName());
                    File parentDir = new File(parentPathBuilder.toString());
                    currentPathText.setText(originItem.getName());
                    if (backFlag == false){
                        backBtn.setText("返回");
                        backBtn.setVisibility(View.VISIBLE);
                        backFlag = true;
                    }
                    showList(parentDir);
                    mainAdapter.notifyDataSetChanged();
                    mainList.setSelection(0);
                    parentPathBuilder = null;
                    parentDir = null;
                    break;
                }
            }
        }
    };

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.back_btn:{
                    backBehindPage();
                    break;
                }
                default:
                    break;
            }
        }
    };

    private final MainHandler mainHandler = new MainHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initial();
    }

    @Override
    public void onBackPressed() {
        if (backFlag == true){
            backBehindPage();
        } else {
            super.onBackPressed();
        }
    }

    private void findView(){
        mainList = (ListView) findViewById(R.id.file_list);
        backBtn = (TextView) findViewById(R.id.back_btn);
        currentPathText = (TextView) findViewById(R.id.current_path_text);
    }

    private void setClickListener(){
        mainList.setOnItemClickListener(itemClickListener);
        mainList.setOnItemLongClickListener(itemLongClickListener);
        backBtn.setOnClickListener(clickListener);
    }


    private void initial(){
        findView();
        longClickDialog = new LongClickDialog();
        longClickDialog.setHandler(mainHandler);
        File file = new File(originPath);
        showList(file);
        backBtn.setVisibility(View.INVISIBLE);
        if (mainListItems != null && mainListItems.size() > 0){
            mainAdapter = new MainListAdapter(mainListItems, MainActivity.this);
            mainList.setAdapter(mainAdapter);
            setClickListener();
        }
    }

    //显示当前目录下的所有文件及文件夹
    private void showList(File parentDir){
        fileListItems.clear();
        dirListItems.clear();
        mainListItems.clear();
        listFiles = parentDir.list();

        String parentPath = parentDir.getAbsolutePath();
        //防止使用相同的引用
        currentPath = new StringBuilder(parentPath).toString();

        if (listFiles.length > 0){
            for (String s : listFiles){
                StringBuilder currentPathBuilder = new StringBuilder(parentPath);
                //检查每一个子文件或文件夹
                currentPathBuilder.append("/");
                currentPathBuilder.append(s);
                File file = new File(currentPathBuilder.toString());
                //过滤所有以.开头的文件和文件夹
                if (s.indexOf(".") != 0){
                    if (file.isDirectory()){
                        DirListItem dirItem = (DirListItem) dirFactory.generateItem(s, parentPath, OriginItem.Type.DIR);
                        dirItem.setDirCounts(file.listFiles(dirFilter).length);
                        dirItem.setFileCounts(file.listFiles(fileFilter).length);
                        dirItem.setIcon(R.mipmap.dir);
                        dirListItems.add(dirItem);
                    } else if (file.isFile()){
                        FileListItem fileItem = (FileListItem) fileFactory.generateItem(s, parentPath, OriginItem.Type.FILE);
                        long length = file.length();
                        if (length < 1024){
                            fileItem.setFileSize(length + "B");
                        }else if (length >= 1024 && length < 1048576){
                            length /= 1024;
                            fileItem.setFileSize(length + "KB");
                        } else {
                            length /= 1048576;
                            fileItem.setFileSize(length + "MB");
                        }
                        String suffix = s.substring(s.lastIndexOf(".") + 1);
                        fileItem.setFileType(suffix);
                        setFileIcon(fileItem, suffix);
                        fileListItems.add(fileItem);
                    }
                }
                file = null;
                currentPathBuilder = null;
            }
            if (fileListItems.size() > 0 && dirListItems.size() > 0){
                dirListItems.addAll(fileListItems);
                mainListItems.addAll(dirListItems);
            }else if (fileListItems.size() > 0){
                mainListItems.addAll(fileListItems);
            }else if (dirListItems.size() > 0){
                mainListItems.addAll(dirListItems);
            }
        } else {
            Toast.makeText(MainActivity.this,"当前目录为空",Toast.LENGTH_SHORT).show();
        }
    }

    private void setFileIcon(FileListItem fileItem, String suffix){
        if (suffix.equals("txt") || suffix.equals("log")){
            fileItem.setIcon(R.mipmap.txt);
        } else if (suffix.equals("doc") || suffix.equals("docx")){
            fileItem.setIcon(R.mipmap.word);
        } else if (suffix.equals("ppt") || suffix.equals("pptx")){
            fileItem.setIcon(R.mipmap.ppt);
        } else if (suffix.equals("xls") || suffix.equals("xlsx")){
            fileItem.setIcon(R.mipmap.xls);
        } else if (suffix.equals("exe")) {
            fileItem.setIcon(R.mipmap.win);
        } else if (suffix.equals("zip") || suffix.equals("rar")){
            fileItem.setIcon(R.mipmap.ziprar);
        } else if (suffix.equals("pdf")){
            fileItem.setIcon(R.mipmap.pdf);
        } else if (suffix.equals("psd")){
            fileItem.setIcon(R.mipmap.psd);
        } else if (suffix.equals("apk")){
            fileItem.setIcon(R.mipmap.apk);
        } else if (suffix.equals("mp3") || suffix.equals("amr")){
            fileItem.setIcon(R.mipmap.music);
        } else if (suffix.equals("jpg") || suffix.equals("png")){
            fileItem.setIcon(R.mipmap.pic);
        } else if(suffix.equals("mp4") || suffix.equals("rmvb")) {
            fileItem.setIcon(R.mipmap.video);
        } else {
            fileItem.setIcon(R.mipmap.unknown);
        }
    }

    private void backBehindPage(){
        StringBuilder backBuilder = new StringBuilder(currentPath);
        backBuilder.delete(backBuilder.lastIndexOf("/"), backBuilder.length());
        File backPath = new File(backBuilder.toString());
        if (backBuilder.toString().equals(originPath)){
            backBtn.setVisibility(View.INVISIBLE);
            currentPathText.setText("文件目录");
            backFlag = false;
        } else {
            currentPathText.setText(backPath.getName());
        }
        showList(backPath);
        mainAdapter.notifyDataSetChanged();
        --currentPoistion;
        mainList.setSelection(clickOrder[currentPoistion]);
        backPath = null;
    }


    private static class MainHandler extends Handler{
        private  MainActivity mActivity;

        private View view;

        private int position;

        private MainHandler(MainActivity mActivity) {
            this.mActivity = mActivity;
        }

        private LinearLayout progressBarLayout;

        private NumberProgressBar progressBar;

        private TextView transferTitle;

        private void setItemView(View view, int position){
            this.view = view;
            this.position = position;
            progressBarLayout = (LinearLayout) view.findViewById(R.id.item_progress_layout);
            progressBar = (NumberProgressBar) view.findViewById(R.id.item_progress_bar);
            transferTitle = (TextView) view.findViewById(R.id.item_transport_title);
        }
        @Override
        public void handleMessage(Message msg) {
            if (mActivity == null){
                super.handleMessage(msg);
                return;
            }
            switch (msg.what){
                case 0:{
                    mainAdapter.setItemState(position, true);
                    progressBarLayout.setVisibility(View.VISIBLE);
                    transferTitle.setVisibility(View.VISIBLE);
                }break;
                case 1: {
                    int a = (int)msg.obj;
                    progressBar.incrementProgressBy(a);
                }break;
                case 2: {
                    progressBarLayout.setVisibility(View.GONE);
                    progressBar.setProgress(0);
                    transferTitle.setText("传输完成");
                    try {
                        Thread.sleep(1000);
                        transferTitle.setVisibility(View.GONE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mainAdapter.setItemState(position, false);
                }
                default:
                    break;
            }
        }
    }

}
