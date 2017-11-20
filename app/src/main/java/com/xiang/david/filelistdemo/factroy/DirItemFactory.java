package com.xiang.david.filelistdemo.factroy;

import com.xiang.david.filelistdemo.model.DirListItem;
import com.xiang.david.filelistdemo.model.OriginItem;

/**
 * Created by Administrator on 2017/11/20.
 */

public class DirItemFactory implements OriginalItemFactory{
    @Override
    public OriginItem generateItem(String name, String absolutePath, OriginItem.Type type) {
        OriginItem item = new DirListItem();
        item.setName(name);
        item.setAbsolutePath(absolutePath);
        item.setType(type);
        return item;
    }
}
