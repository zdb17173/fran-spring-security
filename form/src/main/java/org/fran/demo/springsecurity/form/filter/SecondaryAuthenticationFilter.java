package org.fran.demo.springsecurity.form.filter;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 两步验证的认证Filter
 * @author qiushi
 * @date 2023/5/16
 */
public class SecondaryAuthenticationFilter extends OncePerRequestFilter {

    private RequestMatcher requiresInterceptionRequestMatcher =
            new AntPathRequestMatcher("/login*", "POST");
    private String captchaParameter = "verifyCode";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 不需要拦截当前请求，则放行
        if (!requiresInterception(request)) {
            chain.doFilter(request, response);
            return;
        }

        try{
            checks(request, response);
            request.getSession().setAttribute("verifyCodeError", null);
        }catch (Exception e){
            request.getSession().setAttribute("verifyCodeError", "error");
            response.sendRedirect("/login");
            return;
        }

        // 二级认证通过，则放行当前请求
        chain.doFilter(request, response);
    }

    //校验session中得图形校验码与用户传的验证码是否一致
    public void checks(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        final String captchaCode = (String) request.getSession().getAttribute(captchaParameter);
        Assert.notNull(captchaCode, "请重新获取图形验证码。");

        String presentedCaptchaCode = request.getParameter(captchaParameter);
        if (!captchaCode.equals(presentedCaptchaCode)) {
            Assert.notNull(null, "您输入的图形验证码有误。");
        }
    }

    private boolean requiresInterception(HttpServletRequest request) {
        return this.requiresInterceptionRequestMatcher.matches(request);
    }
}