package org.fran.demo.springsecurity.jwttoken.controller;

import org.fran.demo.springsecurity.jwttoken.service.CaptchaStoreService;
import org.fran.demo.springsecurity.jwttoken.util.CaptchaGeneratorUtil;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class TestController {
    private String verifyCodeParameter = "verifyCode";
    @Resource
    CaptchaStoreService captchaStoreService;

    @RequestMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response) {
        return "/login";
    }

    @GetMapping("/code/image")
    public void createCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = CaptchaGeneratorUtil.createRandomCode(4);
        captchaStoreService.save(code, code);
        //request.getSession().setAttribute(verifyCodeParameter, code);
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