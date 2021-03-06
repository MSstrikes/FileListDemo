package com.xiang.david.filelistdemo.view;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.xiang.david.filelistdemo.R;
import com.xiang.david.filelistdemo.network.FileTransferClient;

/**
 * Created by msstrike on 2017/11/24.
 */

public class LongClickDialog extends DialogFragment{

    private Button sendBtn;
    private View view;
    private String filePath;
    private FileTransferClient transfer;

    //这里复写oncreate方法是为了改变dialog的样式，其余的功能应该在onCreateView里面添加
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filePath = getArguments().getString("filePath");
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.long_click_dialog, container);
        initial();
        return view;
    }
    private void initial(){
        findView();
        setClickListener();
    }
    private void findView(){
        sendBtn = (Button)view.findViewById(R.id.send_file);
    }
    private void setClickListener(){
        sendBtn.setOnClickListener(sendBtnClickListener);
    }

    View.OnClickListener sendBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //向队列添加要传送的文件实体
            transfer.getTransFilesQueue().add(filePath);
            dismiss();
        }
    };

    public void setTransfer(FileTransferClient transfer){
        this.transfer = transfer;
    }
}
