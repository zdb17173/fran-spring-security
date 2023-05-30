package org.fran.demo.springsecurity.jwttoken.config;

import org.fran.demo.springsecurity.jwttoken.filter.JwtJsonUsernamePasswordAuthenticationFilter;
import org.fran.demo.springsecurity.jwttoken.filter.SecondaryAuthenticationFilter;
import org.fran.demo.springsecurity.jwttoken.service.CaptchaStoreService;
import org.fran.demo.springsecurity.jwttoken.service.JwtTokenSecurityContextRepository;
import org.fran.demo.springsecurity.jwttoken.service.JwtTokenService;
import org.fran.demo.springsecurity.jwttoken.service.JwtTokenStoreService;
import org.fran.demo.springsecurity.jwttoken.user.UserModelDetailsService;
import org.fran.demo.springsecurity.jwttoken.util.JsonUtil;
import org.fran.demo.springsecurity.jwttoken.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qiushi
 * @date 2023/5/18
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true,securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${security.captcha.enable}")
    String captchaEnable;

    @Override
    @SuppressWarnings("unchecked")
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
            .and()
            .csrf().disable();

        http
            .formLogin()
            .loginPage("/login");

        JwtJsonUsernamePasswordAuthenticationFilter jsonNamePwdFilter = new JwtJsonUsernamePasswordAuthenticationFilter();
        jsonNamePwdFilter.setAuthenticationManager(authenticationManager());
        jsonNamePwdFilter.setAuthenticationSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
            UserModelDetailsService.CustomUser user = (UserModelDetailsService.CustomUser)authentication.getPrincipal();
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getUid());
            claims.put("userName", user.getUsername());
            String token = jwtTokenService.createToken(claims);
            List<String> permissions = new ArrayList<>();
            user.getAuthorities().forEach(s -> permissions.add(s.getAuthority()));
            jwtTokenStoreService.login(user.getUid(), user.getUsername(), permissions, token);

            Map<String, Object> map = new HashMap<>();
            map.put("user", user);
            map.put("token", token);

            response(JsonUtil.to(map), httpServletResponse);
        });
        jsonNamePwdFilter.setAuthenticationFailureHandler((httpServletRequest, httpServletResponse, e) -> {
            response(TOKEN_LOGIN_FAIL_JSON, httpServletResponse);
        });
        jsonNamePwdFilter.setFilterProcessesUrl("/loginApi");
        http.addFilterAt(jsonNamePwdFilter, UsernamePasswordAuthenticationFilter.class);

        //登出处理
        http.logout().logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
            String token = jwtTokenService.getToken(httpServletRequest);
            if(token!= null)
                jwtTokenStoreService.logout(token);

            response(TOKEN_LOGOUT_SUCCESS_JSON, httpServletResponse);
        });

        http.exceptionHandling()
            //未登录情况下的处理
            .authenticationEntryPoint((httpServletRequest, httpServletResponse, e) -> {
                response(TOKEN_AUTHENTICATION_ENTRY_POINT_JSON, httpServletResponse);
            })
            //登录后访问被denied的处理
            .accessDeniedHandler((httpServletRequest, httpServletResponse, e) -> {
                response(TOKEN_DENY_JSON, httpServletResponse);
            });

        //访问控制
        http.authorizeRequests()
            //放行接口
            .antMatchers("/login", "/loginApi", "/code/image").permitAll()
            //其余接口全部拦截
            .anyRequest().authenticated();

        //使用jwtToken，关闭session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        //jwt上下文管理
        http.securityContext().securityContextRepository(jwtTokenSecurityContextRepository);

        //两步验证
        if(captchaEnable!= null && captchaEnable.equals("true")){
            SecondaryAuthenticationFilter secondaryAuthenticationFilter = new SecondaryAuthenticationFilter(captchaStoreService);
            http.addFilterBefore(secondaryAuthenticationFilter, JwtJsonUsernamePasswordAuthenticationFilter.class);
        }
    }

    public void response(String body, HttpServletResponse httpServletResponse){
        httpServletResponse.setContentType("application/json;charset=utf-8");
        try (PrintWriter out = httpServletResponse.getWriter()){
            out.write(body);
            out.flush();
        } catch (IOException e) {
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return SecurityUtils.encryptPassword((String) rawPassword);
            }
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return SecurityUtils.matchedPassword((String) rawPassword, encodedPassword);
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Resource
    JwtTokenService jwtTokenService;
    @Resource
    JwtTokenSecurityContextRepository jwtTokenSecurityContextRepository;
    @Resource
    JwtTokenStoreService jwtTokenStoreService;
    @Resource
    CaptchaStoreService captchaStoreService;

    public static final String TOKEN_LOGIN_FAIL_JSON = "{\"errorCode\":400 \"errorMessage\":\"登录失败\"}";

    public static final String TOKEN_LOGOUT_SUCCESS_JSON = "{\"errorCode\":0 \"errorMessage\":\"\"}";

    public static final String TOKEN_AUTHENTICATION_ENTRY_POINT_JSON = "{\"errorCode\":403 \"errorMessage\":\"请登录\"}";

    public static final String TOKEN_DENY_JSON = "{\"errorCode\":401 \"errorMessage\":\"访问被拒绝\"}";

}
