package org.fran.demo.springsecurity.jwttoken.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author qiushi
 * @date 2023/5/15
 */
public class SecurityUtils {

    public static String encryptPassword(String password){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    public static boolean matchedPassword(String password, String cryptPwd){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(password, cryptPwd);
    }
}
