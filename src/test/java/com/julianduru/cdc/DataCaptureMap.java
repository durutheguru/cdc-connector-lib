package com.julianduru.cdc;

import com.julianduru.cdc.data.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * created by Julian Duru on 16/05/2023
 */
@Component
public class DataCaptureMap {


    private ConcurrentHashMap<String, Payload> map = new ConcurrentHashMap<>();


    public void put(String key, Payload payload) {
        map.put(key, payload);
    }


    public Payload get(String key) {
        return map.get(key);
    }


    public void remove(String key) {
        map.remove(key);
    }


    public boolean containsKey(String key) {
        return map.containsKey(key);
    }


    public int size() {
        return map.size();
    }


    public void clear() {
        map.clear();
    }


    public boolean isEmpty() {
        return map.isEmpty();
    }


    public ConcurrentHashMap<String, Payload> getMap() {
        return map;
    }


}

