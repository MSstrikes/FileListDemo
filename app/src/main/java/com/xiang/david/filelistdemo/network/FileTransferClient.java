package com.xiang.david.filelistdemo.network;


import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FileTransferClient extends Thread{
    private File sendFile;
    private Socket socket;
    private RandomAccessFile accessFile;
    private DataOutputStream dout;
    private DataInputStream din;
    private long startPointer;
    private long totalSize;
    private int sendSize = 0;
    private Handler handler;
    private LinkedBlockingQueue<String> transFilesQueue = new LinkedBlockingQueue<>();
    private String address;
    private int portNum;
    private ExecutorService service = null;
    public FileTransferClient(Handler handler) {
        this.handler = handler;
    }

    public void connectServer(String address, int portNum){
        this.address = address;
        this.portNum = portNum;
        if (service == null){
            service = Executors.newCachedThreadPool();
        }
        service.execute(doConnect);
    }
    private Runnable doConnect = new Runnable() {
        @Override
        public void run() {
            int count = 0;
            Message msg = new Message();
            msg.what = 3;
            while (!Thread.interrupted()){
                if (count == 10){
                    System.out.println("服务器连接失败，退出");
                    msg.arg1 = 0;
                    break;
                }
                try {
                    socket = new Socket(address, portNum);
                    if (socket.isConnected()){
                        System.out.println("连接建立完成！");
                        msg.arg1 = 1;
                        handler.sendMessage(msg);
                        break;
                    }
                } catch (IOException e) {
                    try {
                        System.out.println("没有能够成功连接服务器，尝试重连");
                        Thread.sleep(3000);
                        count++;
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    };
    @Override
    public void run() {
        while(!Thread.interrupted()){
            try {
                String item = transFilesQueue.take();
                dout = new DataOutputStream(socket.getOutputStream());
                din = new DataInputStream(socket.getInputStream());
                sendFile = new File(item);
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
                float totalF = (float) total;
                int progress, lastProgress = 0;
                byte[] bytes = new byte[1024];
                int length;
                sendSize = 0;
                while ((length = accessFile.read(bytes, 0 , bytes.length)) != -1){
                    dout.write(bytes);
                    sendSize += length;
                    float sendSizeF = (float) sendSize;
                    float progressF = sendSizeF / totalF;
                    progress = (int)(progressF * 100);
                    if (progress != lastProgress){
                        sendProgress(progress);
                        lastProgress = progress;
                    }
                }
                System.out.println("传输完成");
                sendSuccess();
                sendFile = null;
                din.close();
                dout.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        msg.obj = sendFile.getName();
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
        msg.arg1 = transFilesQueue.size();
        handler.sendMessage(msg);

    }
    private void closeSocket(){
        if (socket.isConnected()){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public LinkedBlockingQueue<String> getTransFilesQueue() {
        return transFilesQueue;
    }
}
