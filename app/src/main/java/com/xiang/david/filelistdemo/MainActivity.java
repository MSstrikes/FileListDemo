package com.xiang.david.filelistdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.xiang.david.filelistdemo.factroy.ListItemFactory;
import com.xiang.david.filelistdemo.model.DirListItem;
import com.xiang.david.filelistdemo.model.FileListItem;
import com.xiang.david.filelistdemo.model.OriginItem;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    String[] listFiles = null;
    ListView fileList = null;
    EditText listEdit = null;
    Button listBtn = null;
    String originPath = "/mnt/sdcard/";
    ListItemFactory factory = new ListItemFactory();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initial();
    }
    private void findView(){
        listEdit = (EditText) findViewById(R.id.list_edit);
        fileList = (ListView) findViewById(R.id.file_list);
        listBtn = (Button) findViewById(R.id.list_btn);
    }

    private void setClickListener(){

    }
    private void initial(){
        findView();
        File file = new File(originPath);
        showList(file);
        setClickListener();
    }
    private void showList(File parentDir){
        listFiles = parentDir.list();
        String parentPath = parentDir.getAbsolutePath();
        StringBuilder currentPathBuilder = new StringBuilder(parentPath);
        currentPathBuilder.append("/");
        if (listFiles.length > 0){
            for (String s : listFiles){
                currentPathBuilder.append(s);
                File file = new File(currentPathBuilder.toString());
                if (s.indexOf(".") != 0){
                    if (file.isDirectory()){
                        OriginItem item = factory.generateItem(s, parentPath, OriginItem.Type.DIR);
                        DirListItem dirItem = (DirListItem) item;
                        dirItem.setDirCounts(file.list().length - file.listFiles().length);
                        dirItem.setFileCounts(file.list().length);
                        dirItem.setIcon(R.mipmap.dir);
                    } else if (file.isFile()){
                        OriginItem item = factory.generateItem(s, parentPath, OriginItem.Type.FILE);
                        FileListItem fileItem = (FileListItem) item;
                        fileItem.setFileSize(file.length() / 1048576);
                        String suffix = s.substring(s.lastIndexOf(".") + 1);
                        fileItem.setFileType(suffix);
                        setFileIcon(fileItem, suffix);
                    }
                }
            }
        } else {
            Toast.makeText(MainActivity.this,"当前目录为空",Toast.LENGTH_SHORT).show();
        }
    }
    private void setFileIcon(FileListItem fileItem, String suffix){
        if (suffix.equals("txt")){
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
        } else if (suffix.equals("mp3") || suffix.equals("wma")){
            fileItem.setIcon(R.mipmap.music);
        } else if (suffix.equals("jpg") || suffix.equals("png")){
            fileItem.setIcon(R.mipmap.pic);
        } else {
            fileItem.setIcon(R.mipmap.unknown);
        }
    }
}
