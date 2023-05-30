package org.fran.demo.springsecurity.jwttoken.service;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author qiushi
 * @date 2023/5/29
 */
public class CaptchaStoreService {
    private RedisTemplate redisTemplate;
    private String redisPrefix;
    private int expireSecond;
    private boolean enable;
    private StoreHandler<String> storeHandler;

    public void init(RedisTemplate redisTemplate,
                     String storeType,
                     String redisPrefix,
                     int expireSecond,
                     boolean enable){
        this.redisTemplate = redisTemplate;
        this.redisPrefix = redisPrefix;
        this.expireSecond = expireSecond;
        this.enable = enable;

        if(storeType.equals("redis")){
            storeHandler = new RedisStoreHandler(redisTemplate, redisPrefix, expireSecond);
        } else if(storeType.equals("memory")) {
            storeHandler = new MemoryStoreHandler(expireSecond);
        }else {
            //默认都使用内存存储（有单点问题）
            storeHandler = new MemoryStoreHandler(expireSecond);
        }
    }

    public void save(String key, String code){
        if(!enable)
            return;

        StoreHandler.StoreData<String> data = new StoreHandler.StoreData<>();
        data.setData(code);
        storeHandler.save(key, data);
    }

    public void remove(String key){
        if(!enable)
            return;

        storeHandler.remove(key);
    }

    public String get(String key){
        if(!enable)
            return null;

        String code = storeHandler.get(key);
        return code;
    }

    public class RedisStoreHandler implements StoreHandler<String>{
        private long expireSecond;
        String keyPrefix;
        RedisTemplate redisTemplate;
        public RedisStoreHandler(RedisTemplate redisTemplate, String keyPrefix, long expireSecond){
            this.expireSecond = expireSecond;
            this.keyPrefix = keyPrefix;
            this.redisTemplate = redisTemplate;
        }

        @Override
        public void save(String key, StoreData<String> data) {
            redisTemplate.opsForValue()
                    .set(keyPrefix +":" + key, data.getData(), expireSecond, TimeUnit.SECONDS);
        }

        @Override
        public void remove(String key) {
            redisTemplate.delete(keyPrefix +":" + key);
        }

        @Override
        public String get(String key) {
            Object t = redisTemplate.opsForValue().get(keyPrefix +":" + key);
            if(t!= null) {
                return t.toString();
            }
            return null;
        }
    }

    public class MemoryStoreHandler implements StoreHandler<String>{

        ConcurrentHashMap<String, StoreData<String>> cache = new ConcurrentHashMap<>();
        private long expireTime;

        public MemoryStoreHandler(long expireSecond){
            this.expireTime = expireSecond * 1000l;
        }

        @Override
        public void save(String token, StoreData<String> data) {
            if(data.getExpireTime() == 0)
                data.setExpireTime(System.currentTimeMillis() + expireTime);
            cache.put(token, data);
        }

        @Override
        public void remove(String token) {
            cache.remove(token);
        }

        @Override
        public String get(String token) {
            StoreData<String> data = cache.get(token);
            if(data == null)
                return null;

            if(data.getExpireTime() < System.currentTimeMillis())
                return null;
            else
                return data.getData();
        }
    }
}
