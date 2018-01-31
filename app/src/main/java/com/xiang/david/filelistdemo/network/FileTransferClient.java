package com.xiang.david.filelistdemo.network;


import android.os.Handler;
import android.os.Message;

import com.xiang.david.filelistdemo.model.TransferItem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FileTransferClient extends Thread{
    private String address;
    private int portNum;
    private String filePath;
    private File sendFile;
    private Socket socket;
    private RandomAccessFile accessFile;
    private DataOutputStream dout;
    private DataInputStream din;
    private long startPointer;
    private long totalSize;
    private int sendSize = 0;
    private Handler handler;
    private LinkedBlockingQueue<TransferItem> transFilesQueue = new LinkedBlockingQueue<>();
    public FileTransferClient(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()){

        }
        /*try {
            socket = new Socket(address, portNum);
            dout = new DataOutputStream(socket.getOutputStream());
            din = new DataInputStream(socket.getInputStream());
            System.out.println("连接建立完成！");
            sendFile = new File(filePath);
            accessFile = new RandomAccessFile(sendFile, "rw");
            totalSize = accessFile.length();
            //获取当前进度再发送更好
            dout.writeUTF(sendFile.getName());
            //dout.writeLong(0);
            dout.writeLong(totalSize);
            startPointer = 0;
            //startPointer = din.readLong();
            accessFile.skipBytes((int)startPointer);
            startTransfer();
            int total = (int)accessFile.length();
            int progress, lastProgress = 0;
            byte[] bytes = new byte[1024];
            int length;
            while ((length = accessFile.read(bytes, 0 , bytes.length)) != -1){
                dout.write(bytes);
                sendSize += length;
                progress = (100 * sendSize) / total;
                if (progress != lastProgress){
                    sendProgress(progress);
                    lastProgress = progress;
                }
            }
            System.out.println("传输完成");
            sendSuccess();
        } catch (IOException e) {
            //应该保存当前进度
        }finally {
            try {
                dout.close();
                din.close();
                socket.close();
                accessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }
    private void startTransfer(){
        Message msg = new Message();
        msg.what = 0;
        handler.sendMessage(msg);

    }
    private void sendProgress(int progress){
        Message msg = new Message();
        msg.what = 1;
        msg.obj = progress;
        handler.sendMessage(msg);
    }

    private void sendSuccess(){
        Message msg = new Message();
        msg.what = 2;
        handler.sendMessage(msg);
    }
}
