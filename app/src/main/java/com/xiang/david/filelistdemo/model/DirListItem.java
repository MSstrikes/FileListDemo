package com.xiang.david.filelistdemo.model;

/**
 * Created by msstrike on 2017/11/18.
 */

public class DirListItem extends OriginItem {
    private int fileCounts = 0;
    private int dirCounts = 0;

    public void setFileCounts(int fileCounts) {
        this.fileCounts = fileCounts;
    }

    public void setDirCounts(int dirCounts) {
        this.dirCounts = dirCounts;
    }

    public int getFileCounts() {
        return fileCounts;
    }

    public int getDirCounts() {
        return dirCounts;
    }
}
