package com.lexoff.animediary;

import com.lexoff.animediary.Info.Info;

public class LruCache {
    private static LruCache instance;

    private android.util.LruCache<String, CacheData> cache;

    private int cacheSize = 100 * 1024 * 1024; //100MB
    private int defaultExpireTimeout=1000*60*15; //15 min

    private LruCache(){
        cache=new android.util.LruCache<>(cacheSize);
    }

    public static LruCache getInstance() {
        return instance==null ? instance=new LruCache() : instance;
    }

    public Info getInfo(String key){
        synchronized (cache) {
            return cache.get(key).info;
        }
    }

    public void addInfo(String key, Info info){
        synchronized (cache){
            CacheData data=new CacheData(info, defaultExpireTimeout);

            cache.put(key, data);
        }
    }

    public boolean exist(String key){
        synchronized (cache){
            CacheData data=cache.get(key);
            if (data!=null && data.isExpired()){
                delete(key);
            }

            return cache.get(key)!=null;
        }
    }

    public void clear(){
        synchronized (cache){
            cache.evictAll();
        }
    }

    public void delete(String key){
        synchronized (cache){
            cache.remove(key);
        }
    }

    private static class CacheData {
        private long expire;
        private Info info;

        private CacheData(Info info, long timeout) {
            this.expire = System.currentTimeMillis() + timeout;
            this.info = info;
        }

        private boolean isExpired() {
            return System.currentTimeMillis() > expire;
        }
    }

}
