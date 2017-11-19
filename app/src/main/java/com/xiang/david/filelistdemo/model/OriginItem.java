package com.xiang.david.filelistdemo.model;

/**
 * Created by msstrike on 2017/11/18.
 */



public class OriginItem {
    private String name = null;
    private String absolutePath = null;
    private int icon = 0;
    private Type type = null;
    public void setName(String name) {
        this.name = name;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public int getIcon() {
        return icon;
    }

    public Type getType() {
        return type;
    }

    public enum Type{
        FILE,DIR
    }
}
