package com.xiang.david.filelistdemo.factroy;

import com.xiang.david.filelistdemo.model.OriginItem;

/**
 * Created by msstrike on 2017/11/18.
 */

public interface OriginalItemFactory {
    OriginItem generateItem(String name, String absolutePath, OriginItem.Type type);
}