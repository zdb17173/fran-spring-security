package org.fran.demo.springsecurity.form.config;

import org.fran.demo.springsecurity.form.filter.SecondaryAuthenticationFilter;
import org.fran.demo.springsecurity.form.util.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author qiushi
 * @date 2023/5/18
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true,securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    @SuppressWarnings("unchecked")
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
            .and()
            .csrf().disable();

        http.formLogin()
            .loginPage("/login")
            .loginProcessingUrl("/loginApi")
            .usernameParameter("username")
            .passwordParameter("password")
            //登录成功处理
            .successHandler((httpServletRequest, httpServletResponse, authentication) -> {
                httpServletRequest.getSession().setAttribute("loginError", null);
                httpServletResponse.sendRedirect("/index");
            })
            //登录失败处理
            .failureHandler((httpServletRequest, httpServletResponse, e) -> {
                httpServletRequest.getSession().setAttribute("loginError", e.getMessage());
                httpServletResponse.sendRedirect("/login");
            });

        //登出处理
        http.logout().logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
            httpServletResponse.sendRedirect("/login");
        });


        //访问控制
        http.authorizeRequests()
            //放行接口
            .antMatchers("/login", "/loginApi", "/code/image").permitAll()
            //其余接口全部拦截
            .anyRequest().authenticated();


        //两步验证
        SecondaryAuthenticationFilter secondaryAuthenticationFilter = new SecondaryAuthenticationFilter();
        http.addFilterBefore(secondaryAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
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
}
