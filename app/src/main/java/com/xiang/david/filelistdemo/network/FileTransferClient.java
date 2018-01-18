package com.xiang.david.filelistdemo.network;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

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
    public FileTransferClient(String address, int portNum, String filePath) throws IOException {
        this.address = address;
        this.portNum = portNum;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, portNum);
            dout = new DataOutputStream(socket.getOutputStream());
            din = new DataInputStream(socket.getInputStream());
            System.out.println("连接建立完成！");
            sendFile = new File(filePath);
            System.out.println("filepath" + filePath);
            accessFile = new RandomAccessFile(sendFile, "rw");
            totalSize = accessFile.length();
            //获取当前进度再发送更好
            dout.writeUTF(sendFile.getName());
            dout.writeLong(0);
            dout.writeLong(totalSize);
            startPointer = din.readLong();
            accessFile.skipBytes((int)startPointer);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = accessFile.read(bytes, 0 , bytes.length)) != -1){
                dout.write(bytes);
                sendSize += length;
            }
            System.out.println("发送完成，发送了" + sendSize + "个字节");
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
        }
    }
}
