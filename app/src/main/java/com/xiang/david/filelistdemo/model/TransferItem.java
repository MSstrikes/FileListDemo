package com.xiang.david.filelistdemo.model;

/**
 * Created by Administrator on 2018/1/31 0031.
 */

public class TransferItem {
    private String address = null;
    private int portNum = 0;
    private String filePath = null;
    public TransferItem(String address, int portNum, String filePath){
        this.address = address;
        this.portNum = portNum;
        this.filePath = filePath;
    }

    public String getAddress() {
        return address;
    }

    public int getPortNum() {
        return portNum;
    }

    public String getFilePath() {
        return filePath;
    }
}
