package org.fran.demo.springsecurity.jwttoken.service;

/**
 * @author qiushi
 * @date 2023/5/29
 */
public interface StoreHandler<T> {
    void save(String key, StoreData<T> data);
    void remove(String key);
    T get(String key);

    class StoreData<T>{
        long expireTime;
        T data;

        public long getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }
}
