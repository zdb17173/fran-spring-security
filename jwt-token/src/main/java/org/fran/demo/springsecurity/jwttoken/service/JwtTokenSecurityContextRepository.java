package org.fran.demo.springsecurity.jwttoken.service;

import io.jsonwebtoken.Claims;
import org.fran.demo.springsecurity.jwttoken.user.UserModelDetailsService;
import org.fran.demo.springsecurity.jwttoken.util.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于jwtToken的上下文管理
 * @author qiushi
 * @date 2023/5/18
 */
public class JwtTokenSecurityContextRepository implements SecurityContextRepository {
    JwtTokenService jwtTokenService;
    UserDetailsService userDetailsService;
    JwtTokenStoreService jwtTokenStoreService;

    public JwtTokenSecurityContextRepository(
            JwtTokenStoreService jwtTokenStoreService,
            JwtTokenService jwtTokenService,
            UserDetailsService userDetailsService){
        this.jwtTokenStoreService = jwtTokenStoreService;
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder httpRequestResponseHolder) {
        SecurityContext context = getContext(httpRequestResponseHolder.getRequest());
        if(context == null)
            return SecurityContextHolder.createEmptyContext();
        else
            return context;
    }

    @Override
    public void saveContext(SecurityContext securityContext, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    }

    @Override
    public boolean containsContext(HttpServletRequest httpServletRequest) {
        return getContext(httpServletRequest)!= null;
    }

    public SecurityContext getContext(HttpServletRequest httpServletRequest){
        String token = jwtTokenService.getToken(httpServletRequest);
        if(StringUtils.isEmpty(token))
            return null;

        if(jwtTokenStoreService.isEnable()){
            //从存储中取用户( memory | redis )
            JwtTokenStoreService.SessionData tokenData = jwtTokenStoreService.get(token);
            if(tokenData == null)
                return null;

            SecurityContext context = SecurityContextHolder.createEmptyContext();

            List<String> permissions = tokenData.getPermissions();
            List<GrantedAuthority> authorities = new ArrayList<>();
            if(permissions!= null && permissions.size()>0)
                permissions.forEach(s -> authorities.add(new SimpleGrantedAuthority(s)));

            UserModelDetailsService.CustomUser user = new UserModelDetailsService.CustomUser(
                    tokenData.getUid(),
                    tokenData.getUsername(),
                    null,
                    authorities
            );

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
            context.setAuthentication(authenticationToken);
            return context;
        }else{
            //从userDetailService中取用户（数据库）
            Claims claim = jwtTokenService.parseJwtToken(token);
            if(claim == null)
                return null;

            String userId = claim.get("userId", String.class);
            String userName = claim.get("userName", String.class);
            if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(userName))
                return null;

            UserDetails user = userDetailsService.loadUserByUsername(userName);
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
            context.setAuthentication(authenticationToken);
            return context;
        }
    }
}
