package cache;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存
 */
public class CacheUtil {
    private static class LazyHolder {
        private static final CacheUtil INSTANCE = new CacheUtil();
    }

    public static final CacheUtil getInstance() {
        return CacheUtil.LazyHolder.INSTANCE;
    }

    Cache<String, Object> cache=null;
    private CacheUtil () {
        init();
    }

    /**
     * 初始化
     */
    private  void  init()
    {
        cache = Caffeine.newBuilder()
                //初始数量
                .initialCapacity(10)
                //最大条数
                .maximumSize(10)
                .refreshAfterWrite(1, TimeUnit.MINUTES)
                //expireAfterWrite和expireAfterAccess同时存在时，以expireAfterWrite为准
                //最后一次写操作后经过指定时间过期
                .expireAfterWrite(20, TimeUnit.MINUTES)
                //最后一次读或写操作后经过指定时间过期
                .expireAfterAccess(20, TimeUnit.MINUTES)
                //监听缓存被移除
                .removalListener((key, val, removalCause) -> { })
                //记录命中
                .recordStats()
                .build();
    }


  public  void  put(String key,Object obj)
  {
      cache.put(key,obj);
  }

    public  Object  get(String key)
    {
       return cache.getIfPresent(key);
    }

    public  void  remove(String key)
    {
        cache.invalidate(key);
    }
    public  void  removeAll(List<String> keys)
    {
        cache.invalidateAll(keys);
    }


    /**
     * 保持数据
     * @param key 一级key
     * @param k  二级key
     * @param obj
     */
    public  void  putmap(String key,String k,Object obj)
    {

       Map<String,Object> map=(Map<String,Object>)cache.getIfPresent(key);
       if(map==null)
       {
           map=new HashMap<>();
       }
       map.put(k,obj);
    }

    public  Object  get(String key,String k)
    {
        Map<String,Object> map=(Map<String,Object>)cache.getIfPresent(key);
        if(map==null)
        {
           return  null;
        }
       return   map.get(k);

    }

    public  void  remove(String key,String k)
    {

        Map<String,Object> map=(Map<String,Object>)cache.getIfPresent(key);
        if(map!=null)
        {
            map.remove(k);
        }

    }



}
