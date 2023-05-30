package org.fran.demo.springsecurity.jwttoken.config;

import org.fran.demo.springsecurity.jwttoken.service.CaptchaStoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author qiushi
 * @date 2023/5/29
 */
@Configuration
public class CaptchaConfig {
    @Bean
    CaptchaStoreService captchaStoreService(
            RedisTemplate redisTemplate,
            @Value("${security.storeType}") String storeType,
            @Value("${security.redis.prefix}") String redisPrefix,
            @Value("${security.captcha.expireSecond}") int expireSecond,
            @Value("${security.captcha.enable}") boolean enable){
        CaptchaStoreService css = new CaptchaStoreService();
        css.init(redisTemplate, storeType, redisPrefix, expireSecond, enable);
        return css;
    }
}
