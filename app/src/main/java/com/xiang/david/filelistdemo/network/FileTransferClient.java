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
import java.net.SocketAddress;
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
    private String address, currentTransferItem = null;
    private int portNum;
    private ExecutorService service = null;
    private boolean pause = false;
    private byte[] pauseKey = new byte[0];
    public FileTransferClient(Handler handler, String address, int portNum) {
        this.handler = handler;
        this.address = address;
        this.portNum = portNum;
        if (service == null){
            service = Executors.newCachedThreadPool();
        }
    }

    private Runnable downLinkThread = new Runnable() {
        @Override
        public void run() {
            //接收服务器的下行数据
        }
    };

    private Runnable upLinkThread = new Runnable() {
        @Override
        public void run() {
            //向服务器发送上行数据
            while(!Thread.interrupted()){
                try {
                    if (currentTransferItem == null) currentTransferItem = transFilesQueue.take();
                    dout = new DataOutputStream(socket.getOutputStream());
                    din = new DataInputStream(socket.getInputStream());
                    sendFile = new File(currentTransferItem);
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
                        synchronized (pauseKey){
                            if (pause) pauseKey.wait();
                        }
                        dout.write(bytes,0, length);
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
            try {
                din.close();
                dout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
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
                    service.execute(upLinkThread);
                    service.execute(downLinkThread);
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
        currentTransferItem = null;
    }

    public void pauseTransfer(){
        pause = true;
    }

    public void stopTransfer(){
        service.shutdownNow();
        service = null;
        currentTransferItem = null;
    }

    public void resumeTransfer(){
        synchronized (pauseKey){
            pause = false;
            pauseKey.notifyAll();
        }
    }

    public SocketAddress getSocketInfo(){
        return socket.getRemoteSocketAddress();
    }
    public LinkedBlockingQueue<String> getTransFilesQueue() {
        return transFilesQueue;
    }
}
