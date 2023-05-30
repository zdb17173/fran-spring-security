package org.fran.demo.springsecurity.jwttoken.service;

import org.fran.demo.springsecurity.jwttoken.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * JwtToken的存储
 * @author qiushi
 * @date 2023/5/18
 */
public class JwtTokenStoreService {
    @Autowired(required = false)
    private RedisTemplate redisTemplate;
    @Value("${security.redis.prefix}")
        private String redisPrefix;
    @Value("${security.jwt.expireSecond}")
    private int expireSecond;
    private StoreHandler<SessionData> storeHandler;
    private boolean enable = true;

    public void init(RedisTemplate redisTemplate,
                     String storeType,
                     String redisPrefix,
                     int expireSecond
                     ){

        this.redisTemplate = redisTemplate;
        this.redisPrefix = redisPrefix;
        this.expireSecond = expireSecond;

        if(storeType.equals("redis")){
            storeHandler = new RedisStoreHandler(redisTemplate, redisPrefix, expireSecond);
        }else if(storeType.equals("memory")) {
            storeHandler = new MemoryStoreHandler(expireSecond);
        }else if(storeType.equals("none")) {
            storeHandler = new NoopStoreHandler();
            this.enable = false;
        }
    }

    public boolean isEnable(){
        return enable;
    }

    public void login(String userId, String username, List<String> permissions, String token){
        SessionData sessionData = new SessionData(userId, username, expireSecond, permissions);
        StoreHandler.StoreData<SessionData> data = new StoreHandler.StoreData<>();
        data.setData(sessionData);
        data.setExpireTime(expireSecond);
        storeHandler.save(token, data);
    }

    public void logout(String token){
        storeHandler.remove(token);
    }

    public SessionData get(String token){
        return storeHandler.get(token);
    }

    public static class RedisStoreHandler implements StoreHandler<SessionData>{
        private long expireSecond;
        String keyPrefix;
        RedisTemplate redisTemplate;
        public RedisStoreHandler(RedisTemplate redisTemplate, String keyPrefix, long expireSecond){
            this.expireSecond = expireSecond;
            this.keyPrefix = keyPrefix;
            this.redisTemplate = redisTemplate;
        }

        @Override
        public void save(String token, StoreData<SessionData> data) {
            SessionData sessionData = data.getData();
            redisTemplate.opsForValue()
                    .set(keyPrefix +":" + token, sessionData.toString(), expireSecond, TimeUnit.SECONDS);
        }

        @Override
        public void remove(String token) {
            redisTemplate.delete(keyPrefix +":" + token);
        }

        @Override
        public SessionData get(String token) {
            Object t = redisTemplate.opsForValue().get(keyPrefix +":" + token);
            if(t!= null) {
                SessionData data = SessionData.stringToObj(t.toString());
                return data;
            }else
                return null;
        }
    }

    public static class NoopStoreHandler implements StoreHandler<SessionData>{

        @Override
        public void save(String key, StoreData<SessionData> data) {

        }

        @Override
        public void remove(String key) {

        }

        @Override
        public SessionData get(String key) {
            return null;
        }
    }

    public static class MemoryStoreHandler implements StoreHandler<SessionData>{
        static ConcurrentHashMap<String, StoreData<SessionData>> cache = new ConcurrentHashMap<>();
        private long expireSecond;

        public MemoryStoreHandler(long expireSecond){
            this.expireSecond = expireSecond;
        }

        @Override
        public void save(String key, StoreData<SessionData> data) {
            cache.put(key, data);
        }

        @Override
        public void remove(String key) {
            cache.remove(key);
        }

        @Override
        public SessionData get(String token) {
            StoreData<SessionData> data = cache.get(token);
            if(data == null)
                return null;

            if(data.getExpireTime() < System.currentTimeMillis())
                return null;
            else
                return data.getData();
        }
    }

    public static class SessionData{
        long expireTime;
        List<String> permissions;
        String uid;
        String username;
        public SessionData(){}
        public SessionData(String uid, String username, long expireSecond, List<String> permissions) {
            this.uid = uid;
            this.username = username;
            this.expireTime = new Date().getTime() + expireSecond*1000;
            this.permissions = permissions;
        }

        public long getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String toString(){
            return JsonUtil.to(this);
        }

        public static SessionData stringToObj(String json){
            return JsonUtil.from(json, SessionData.class);
        }
    }
}
