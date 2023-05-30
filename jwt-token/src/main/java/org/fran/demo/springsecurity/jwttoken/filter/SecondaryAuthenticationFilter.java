package org.fran.demo.springsecurity.jwttoken.filter;

import org.fran.demo.springsecurity.jwttoken.service.CaptchaStoreService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 两步验证的认证Filter
 * @author qiushi
 * @date 2023/5/29
 */
public class SecondaryAuthenticationFilter extends OncePerRequestFilter {
    private RequestMatcher requiresInterceptionRequestMatcher =
            new AntPathRequestMatcher("/loginApi", "POST");
    private String captchaParameter = "verifyCode";

    CaptchaStoreService captchaStoreService;

    public SecondaryAuthenticationFilter(CaptchaStoreService captchaStoreService){
        this.captchaStoreService = captchaStoreService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 不需要拦截当前请求，则放行
        if (!requiresInterception(request)) {
            chain.doFilter(request, response);
            return;
        }

        try{
            checks(request, response);
        }catch (Exception e){
            response("{\"errorCode\":400 \"errorMessage\":\""+ e.getMessage() +"\"}", response);
            return;
        }

        // 二级认证通过，则放行当前请求
        chain.doFilter(request, response);
    }

    //校验session中得图形校验码与用户传的验证码是否一致
    public void checks(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String presentedCaptchaCode = request.getParameter(captchaParameter);
        Assert.notNull(presentedCaptchaCode, "输入的图形码为空。");

        final String captchaCode = (String) captchaStoreService.get(presentedCaptchaCode);
        Assert.notNull(captchaCode, "请重新获取图形验证码。");

        if (!captchaCode.equals(presentedCaptchaCode)) {
            Assert.notNull(null, "您输入的图形验证码有误。");
        }

        captchaStoreService.remove(presentedCaptchaCode);
    }

    private boolean requiresInterception(HttpServletRequest request) {
        return this.requiresInterceptionRequestMatcher.matches(request);
    }

    public void response(String body, HttpServletResponse httpServletResponse){
        httpServletResponse.setContentType("application/json;charset=utf-8");
        try (PrintWriter out = httpServletResponse.getWriter()){
            out.write(body);
            out.flush();
        } catch (IOException e) {
        }
    }
}