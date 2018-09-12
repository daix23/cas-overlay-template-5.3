package com.zjasm.controller;

import com.zjasm.captcha.CaptchaUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
public class RegController {

    @GetMapping("test")
    public String test(){
        return "Hello World!";
    }


    @GetMapping(value = "captcha")
    public String capcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 设置响应的类型格式为图片格式
        response.setContentType("image/jpeg");
        //禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession();

        CaptchaUtil captchaUtil = new CaptchaUtil(100, 40, 4);
        session.setAttribute("captcha", captchaUtil.getCode());
        captchaUtil.write(response.getOutputStream());
        return null;
    }

}
