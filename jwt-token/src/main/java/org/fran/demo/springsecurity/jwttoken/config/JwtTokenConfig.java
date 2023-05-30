package org.fran.demo.springsecurity.jwttoken.config;

import org.fran.demo.springsecurity.jwttoken.service.JwtTokenSecurityContextRepository;
import org.fran.demo.springsecurity.jwttoken.service.JwtTokenService;
import org.fran.demo.springsecurity.jwttoken.service.JwtTokenStoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;


/**
 * @author qiushi
 * @date 2023/5/18
 */
@Configuration
public class JwtTokenConfig {

    @Bean
    JwtTokenService tokenService(
            @Value("${security.jwt.httpRequestHeader}") String header,
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expireSecond}") int expireSecond){
        return new JwtTokenService(header, secret, expireSecond);
    }

    @Bean
    JwtTokenSecurityContextRepository jwtTokenSecurityContextRepository(
            JwtTokenStoreService jwtTokenStoreService, JwtTokenService jwtTokenService, UserDetailsService userDetailsService
    ){
        return new JwtTokenSecurityContextRepository(jwtTokenStoreService, jwtTokenService, userDetailsService);
    }

    @Bean
    JwtTokenStoreService jwtTokenStoreService(
            RedisTemplate redisTemplate,
            @Value("${security.storeType}") String storeType,
            @Value("${security.redis.prefix}") String redisPrefix,
            @Value("${security.jwt.expireSecond}") int expireSecond
    ){
        JwtTokenStoreService store =  new JwtTokenStoreService();
        store.init(redisTemplate,
                storeType == null? "" : storeType,
                redisPrefix,
                expireSecond);
        return store;
    }
}
