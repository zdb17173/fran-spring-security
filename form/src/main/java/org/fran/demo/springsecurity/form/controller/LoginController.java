package org.fran.demo.springsecurity.form.controller;

import org.fran.demo.springsecurity.form.util.CaptchaGeneratorUtil;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    private String verifyCodeParameter = "verifyCode";

    @RequestMapping("/login")
    public ModelAndView login(HttpServletRequest request) {
        Object verifyCodeError = request.getSession().getAttribute("verifyCodeError");
        Object loginError = request.getSession().getAttribute("loginError");

        Map<String, Object> map = new HashMap<>();
        if(verifyCodeError != null)
            map.put("verifyCodeError", verifyCodeError);
        if(loginError != null)
            map.put("loginError", loginError);

        return new ModelAndView("login", map);
    }

    @RequestMapping("/index")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> map = new HashMap<>();
        map.put("authentication", authentication);
        return new ModelAndView("index", map);
    }

    @GetMapping("/code/image")
    public void createCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = CaptchaGeneratorUtil.createRandomCode(4);
        request.getSession().setAttribute(verifyCodeParameter, code);
        String str = new CaptchaGeneratorUtil().getCertPic(0, 0, code, response.getOutputStream());

    }

    @ResponseBody
    @Secured({"ROLE_editor"})
    @RequestMapping("/test1")
    public String test1(HttpServletRequest request, HttpServletResponse response) {
        return "test1";
    }

    @ResponseBody
    @Secured({"ROLE_editor", "ROLE_copyEditor"})
    @RequestMapping("/test2")
    public String test2(HttpServletRequest request, HttpServletResponse response) {
        return "test2";
    }

    @ResponseBody
    @Secured({"ROLE_editor", "ROLE_copyEditor","ROLE_chief"})
    @RequestMapping("/test3")
    public String test3(HttpServletRequest request, HttpServletResponse response) {
        return "test3";
    }
}