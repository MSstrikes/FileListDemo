package com.xiang.david.filelistdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.xiang.david.filelistdemo.network.FileTransferClient;
import com.xiang.david.filelistdemo.view.LongClickDialog;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
    注意 getAbsolutePath获得的地址最后没有以‘/’结束，必须自己添加
 */
public class MainActivity extends Activity {
    /*
        final类型
     */
    static final String IP_ADDRESS = "192.168.1.42";
    static final int PORT_NUM = 8888;
    static final int OFFLINE = 0;
    static final int ONLINE = 1;
    static final int CONNECT_ERROR = -1;
    static final int BUSY = 1;
    static final int FREE = 0;


    /*
        界面控件类型
    */
    ListView mainList = null;
    TextView backBtn = null;
    TextView currentPathText = null;
    BottomSheetBehavior behavior = null;
    LongClickDialog longClickDialog = null;
    LinearLayout internetStateLayout = null;
    ImageButton startPauseBtn = null;
    /*
        基本数据类型
     */
    String originPath = "/mnt/sdcard";
    String currentPath = null;
    int currentPoistion = 0;
    boolean backFlag = false;
    int[] clickOrder = new int[100];
    String[] listFiles = null;
    public int internetState = OFFLINE;
    public int transferState;
    /*
        工厂类型
     */
    DirItemFactory dirFactory = new DirItemFactory();
    FileItemFactory fileFactory = new FileItemFactory();
    FileIntentFactory fileIntentFactory = new FileIntentFactory();
    /*
        列表类型
     */
    ArrayList<OriginItem> fileListItems = new ArrayList<>();
    ArrayList<OriginItem> dirListItems = new ArrayList<>();
    ArrayList<OriginItem> mainListItems = new ArrayList<>();
    /*
        适配器
     */
    static MainListAdapter mainAdapter = null;

    /*
        过滤器
     */
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

    /*
        监听器接口实现
     */
    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (longClickDialog != null){
                longClickDialog.show(getFragmentManager(), "LongClickDialog");
            } else {
                longClickDialog = new LongClickDialog();
                longClickDialog.setTransfer(transferClient);
            }
            Bundle fileBundle = new Bundle();
            OriginItem item = mainListItems.get(position);
            fileBundle.putString("filePath",item.getAbsolutePath() + "/" + item.getName());
            longClickDialog.setArguments(fileBundle);
            longClickDialog.setCancelable(true);
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
    @Override
    public void onBackPressed() {
        if (backFlag == true){
            backBehindPage();
        } else {
            super.onBackPressed();
        }
    }

    View.OnClickListener layoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (internetState == OFFLINE){
                internetStateLayout.setClickable(false);
                //弹出窗口选择服务器
                transferClient = new FileTransferClient(mainHandler, IP_ADDRESS, PORT_NUM);
                service.execute(transferClient);
            } else if (internetState == ONLINE){
                //弹出窗口确认
                Bundle bundle = new Bundle();
                bundle.putString("address", transferClient.getSocketInfo().toString());

            }
        }
    };

    View.OnClickListener startPauseBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (transferState == BUSY){
                //暂停传输
                transferClient.pauseTransfer();
                transferState = FREE;
                startPauseBtn.setBackgroundResource(R.mipmap.start);
            } else {
                //继续传输
                transferClient.resumeTransfer();
                transferState = BUSY;
                startPauseBtn.setBackgroundResource(R.mipmap.pause);
            }
        }
    };
    /*
        主界面功能、工具类实现
     */
    private MainHandler mainHandler = null;

    FileTransferClient transferClient = null;

    ExecutorService service = Executors.newSingleThreadExecutor();

    /**
     *@functionName onCreate
     *@date on 2018/2/2 0002 15:59
     *@author Xiang
     *@param @savedInstanceState
     *@return   @void
     *@describe activity生命周期起点，用于初始化工作
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initial();
    }

    /**
     *@functionName initial
     *@date on 2018/2/2 0002 15:59
     *@author Xiang
     *@param @null
     *@return @void
     *@describe 进行初始化工作，启动传输线程
     */
    private void initial(){
        findView();
        File file = new File(originPath);
        showList(file);
        backBtn.setVisibility(View.INVISIBLE);
        if (mainListItems != null && mainListItems.size() > 0){
            mainAdapter = new MainListAdapter(mainListItems, MainActivity.this);
            mainList.setAdapter(mainAdapter);
            setClickListener();
        }
    }

    /**
     *@functionName findView
     *@date on 2018/2/2 0002 16:00
     *@author Xiang
     *@param @null
     *@return @void
     *@describe 设置布局绑定控件和适配器
     */
    private void findView(){
        mainHandler = new MainHandler(this);
        mainList = (ListView) findViewById(R.id.file_list);
        backBtn = (TextView) findViewById(R.id.back_btn);
        currentPathText = (TextView) findViewById(R.id.current_path_text);
        internetStateLayout = (LinearLayout) findViewById(R.id.internet_state_layout);
        behavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        mainHandler.setBehavior(behavior);
        startPauseBtn = (ImageButton) findViewById(R.id.bottom_start_pause_btn);
    }

    /**
     *@functionName setClickListener
     *@date on 2018/2/2 0002 16:11
     *@author Xiang
     *@param @null
     *@return @void
     *@describe 绑定监听器
     */
    private void setClickListener(){
        mainList.setOnItemClickListener(itemClickListener);
        mainList.setOnItemLongClickListener(itemLongClickListener);
        backBtn.setOnClickListener(clickListener);
        internetStateLayout.setOnClickListener(layoutClickListener);
        startPauseBtn.setOnClickListener(startPauseBtnClickListener);
    }




    /**
     *@functionName showList
     *@date on 2018/2/2 0002 16:12
     *@author Xiang
     *@param @File parentDir
     *@return @void
     *@describe 将当前目录下的所有文件及文件夹填充至列表中并进行显示
     */
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
    /**
     *@functionName setFileIcon
     *@date on 2018/2/2 0002 16:13
     *@author Xiang
     *@param @FileListItem fileItem @String suffix
     *@return @void
     *@describe 根据每个文件的后缀设置图标
     */
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

    /**
     *@functionName backBehindPage
     *@date on 2018/2/2 0002 16:14
     *@author Xiang
     *@param @null
     *@return @void
     *@describe 回退到上一页
     */
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
        private TextView bottomFileName;
        private TextView bottomSequence;
        private TextView internetStateText;
        private ImageView internetStateImg;
        private LinearLayout internetStateLayout;
        private NumberProgressBar progressBar;
        private MainActivity mActivity;
        private BottomSheetBehavior behavior;
        private MainHandler(MainActivity mActivity) {
            this.mActivity = mActivity;
            bottomFileName = (TextView) mActivity.findViewById(R.id.bottomsheet_filename);
            bottomSequence = (TextView) mActivity.findViewById(R.id.bottomsheet_sequence_num);
            progressBar = (NumberProgressBar) mActivity.findViewById(R.id.bottom_progressbar);
            internetStateText = (TextView) mActivity.findViewById(R.id.internet_state_text);
            internetStateImg = (ImageView) mActivity.findViewById(R.id.internet_state_img);
            internetStateLayout = (LinearLayout) mActivity.findViewById(R.id.internet_state_layout);
        }
        public void setBehavior(BottomSheetBehavior behavior){
            this.behavior = behavior;
        }
        @Override
        public void handleMessage(Message msg) {
            if (mActivity == null){
                super.handleMessage(msg);
                return;
            }
            switch (msg.what){
                case 0:{ //发送开始
                    if (behavior != null){
                        mActivity.transferState = 1;
                        progressBar.setProgress(0);
                        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            bottomFileName.setText(msg.obj.toString());
                        }
                    }
                }break;
                case 1: { //发送中
                    int progress = (int)msg.obj;
                    progressBar.setProgress(progress);
                }break;
                case 2: { //发送完成
                    if (msg.arg1 == 0){ //如果队列中没有要继续传输的文件
                        mActivity.transferState = 0;
                        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            Toast.makeText(mActivity, "传输完成", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(mActivity, "传输完成", Toast.LENGTH_SHORT).show();
                        }
                    } else { //队列中还有要继续传输的文件
                        //队列减少
                        int a = Integer.valueOf(bottomSequence.getText().toString());
                        --a;
                        bottomSequence.setText(a);
                        Toast.makeText(mActivity, bottomFileName.getText().toString() + "传输完成", Toast.LENGTH_SHORT).show();
                    }

                }break;
                case 3:{
                    internetStateLayout.setClickable(true);
                    if (msg.arg1 == CONNECT_ERROR){
                        Toast.makeText(mActivity, "服务器连接失败", Toast.LENGTH_SHORT).show();
                        mActivity.internetState = OFFLINE;

                    } else if (msg.arg1 == ONLINE){
                        internetStateText.setText("已连接");
                        internetStateImg.setImageResource(R.drawable.ic_disconnect);
                        mActivity.internetState = ONLINE;
                    } else {
                        Toast.makeText(mActivity, "已断开", Toast.LENGTH_SHORT).show();
                        internetStateText.setText("未连接");
                        internetStateImg.setImageResource(R.drawable.ic_connect);
                    }
                }break;
                default:
                    break;
            }
        }
    }

}
