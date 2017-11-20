package com.xiang.david.filelistdemo.factroy;

import com.xiang.david.filelistdemo.model.FileListItem;
import com.xiang.david.filelistdemo.model.OriginItem;

/**
 * Created by Administrator on 2017/11/19.
 */

public class FileItemFactory implements OriginalItemFactory{
    @Override
    public OriginItem generateItem(String name, String absolutePath, OriginItem.Type type) {
        OriginItem item = new FileListItem();
        item.setName(name);
        item.setAbsolutePath(absolutePath);
        item.setType(type);
        return item;
    }
}
