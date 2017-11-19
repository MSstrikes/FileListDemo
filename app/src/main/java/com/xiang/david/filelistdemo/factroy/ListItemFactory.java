package com.xiang.david.filelistdemo.factroy;

import com.xiang.david.filelistdemo.model.OriginItem;

/**
 * Created by msstrike on 2017/11/18.
 */

public class ListItemFactory {
    public OriginItem generateItem(String name, String absolutePath, OriginItem.Type type){
        OriginItem item = new OriginItem();
        item.setName(name);
        item.setAbsolutePath(absolutePath);
        return item;
    }
}
