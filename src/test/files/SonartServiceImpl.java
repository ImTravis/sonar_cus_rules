package com.example.demo.service.impl;

import com.example.demo.db.CollectionItem;
import com.example.demo.service.SonartService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


public class SonartServiceImpl {

    /**
     * @param str
     * @return
     * @des hello word getSonarTestHello
     */
 
    public String getSonarTestHello(String str) {
        return "service : hello i am " + str;
    }

    /**
     * @param str
     * @return
     * @des 测试代码getSaveHello
     */
 
    public String getSaveHello(String str) {
//        str = null;
        if (str.equals("travis")) {
            return "service : save i am null ";
        }
        return "service : save i am " + str;
    }


    public List<CollectionItem> getMoreCollectionsO() {
        List<CollectionItem> result = new ArrayList<>();
        for (int k = 0; k < 10; k++) {
            result.addAll(getCollections());
        }
        return result;
    }

 
    public List<CollectionItem> getCollections() {
        List<CollectionItem> result = new ArrayList<>();
        for (int k = 0; k < 10; k++) {
            result.add(dbQuery());
        }
        return result;
    }

 
    public CollectionItem dbQuery() {
        int random = (int) (Math.random() * 10 + 1);
        CollectionItem collectionItem = new CollectionItem();
        String[] types = {"用电", "用煤", "用水"};
        collectionItem.setName(types[(random % (types.length))]);
        int value = (int) (Math.random() * 100 + 1);
        collectionItem.setValue(value);
        return collectionItem;
    }

    public List<CollectionItem> getMoreCollections() {
        List<CollectionItem> result = new ArrayList<>();
        for (int k = 0; k < 10; k++) {
            for (int a = 0; a < 10; a++) {
                result.add(dbQuery());
            }
        }
        return result;
    }

}
