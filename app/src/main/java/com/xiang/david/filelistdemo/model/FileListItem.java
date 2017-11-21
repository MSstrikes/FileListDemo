package com.xiang.david.filelistdemo.model;

/**
 * Created by msstrike on 2017/11/18.
 */

public class FileListItem extends OriginItem{
    private String fileType = null;
    private String fileSize = null;

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getFileType() {
        return fileType;
    }
}
