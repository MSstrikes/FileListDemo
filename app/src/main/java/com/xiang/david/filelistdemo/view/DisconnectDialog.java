package com.xiang.david.filelistdemo.view;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.xiang.david.filelistdemo.R;

/**
 * Created by Administrator on 2018/2/6 0006.
 */

public class DisconnectDialog extends DialogFragment{
    private String remoteAddress = null;
    private View view = null;
    private TextView disconnectText = null;
    private Button disconnectConfirm = null;
    private Button disconnectCancel = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        remoteAddress = getArguments().getString("address");
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.disconnect_dialog, container);
        initial();
        return view;
    }
    private void initial(){
        disconnectText = (TextView) view.findViewById(R.id.disconnect_dialog_text);
        disconnectConfirm = (Button) view.findViewById(R.id.disconnect_dialog_confirm);
        disconnectCancel = (Button) view.findViewById(R.id.disconnect_dialog_cancel);
        disconnectText.setText("是否要与" + remoteAddress + "服务器断开连接？");
        disconnectConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        disconnectCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
