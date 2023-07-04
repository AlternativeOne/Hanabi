package com.lexoff.animediary;

import java.util.HashMap;

public class CustomHashMap extends HashMap<String, Object> {

    public int getInt(Object key){
        return (int) get(key);
    }

    public String getString(Object key){
        return (String) get(key);
    }

}
