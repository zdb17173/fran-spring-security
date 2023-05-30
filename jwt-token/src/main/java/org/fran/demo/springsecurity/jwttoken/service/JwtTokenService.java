package org.fran.demo.springsecurity.jwttoken.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.fran.demo.springsecurity.jwttoken.user.UserModelDetailsService;
import org.fran.demo.springsecurity.jwttoken.util.SecurityUtils;
import org.fran.demo.springsecurity.jwttoken.util.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

/**
 * JwtToken的生成、解析、验证
 * @author qiushi
 * @date 2023/5/17
 */
public class JwtTokenService {

    private final String header;
    private final String secret;
    private final long expireTime;
    private final String TOKEN_PREFIX = "Bearer ";

    private Key key;
    private JwtParser parser;

    public static void main(String[] args){
        JwtTokenService ts = new JwtTokenService("header",
                "Zd+kZozTI5OgURtqbcWS.sD232sdJOOpsdy=TkssdWEEDSJkasHUWEKgsdUWOkkdsauDSDHJklsfFYUGINOIJK", 10);

        List<GrantedAuthority> authorities = new ArrayList<>();
        for(int i =0; i < 10; i++)
        authorities.add(new SimpleGrantedAuthority("ROLE_editor" + i));

        UserModelDetailsService.CustomUser ed = new UserModelDetailsService.CustomUser(
                "1",
                "ed",//user name
                SecurityUtils.encryptPassword((String) "1"),//user pwd
                authorities);

        Map<String, Object> claims = new HashMap<>();
        ed.setPassWord(null);
        claims.put("user", ed);
        String token = ts.createToken(claims);
        System.out.println("token [" + token + "]");
        Object validate = ts.parseJwtToken(token);
        System.out.println("token is validate["+ validate +"]");
        try {
            Thread.sleep(1000*20l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        validate = ts.parseJwtToken(token);
        System.out.println("token is validate["+ validate +"]");
    }

    public JwtTokenService(String header, String secret, long expireTime) {
        this.header = header;
        this.secret = secret;
        this.expireTime = expireTime;

        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        parser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public String createToken(Map<String, Object> claims) {
        long now = (new Date()).getTime();
        long expire = expireTime * 1000l;
        Date validity = new Date(now + expire);;

        return Jwts.builder()
                .setClaims(claims)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    public String getToken(HttpServletRequest request) {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token)) {
            if(token.startsWith(TOKEN_PREFIX))
                token = token.replace(TOKEN_PREFIX, "");
        }
        return token;
    }

    public Claims parseJwtToken(String authToken) {
        try {
            Jws<Claims> claims = parser.parseClaimsJws(authToken);
            return claims.getBody();
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {

        } catch (ExpiredJwtException e) {

        } catch (UnsupportedJwtException e) {

        } catch (IllegalArgumentException e) {

        }
        return null;
    }
}
