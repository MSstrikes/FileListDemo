package com.xiang.david.filelistdemo.model;

/**
 * Created by msstrike on 2017/11/18.
 */

public class FileListItem extends OriginItem{
    private String fileType = null;
    private long fileSize = 0;

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileType() {
        return fileType;
    }
}
