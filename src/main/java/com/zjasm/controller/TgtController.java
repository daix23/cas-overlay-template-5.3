package com.zjasm.controller;

/*import com.zjasm.server.TgtServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/tgt")*/
public class TgtController {

    /*@Autowired
    private TgtServer tgtServer;

    *//**
     * CAS 登录授权
     *//*
    @PostMapping("/getTgtByName")
    public Object getTgtByName(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String username = request.getParameter("username");

        System.out.println("username：" + username );

        // 1、获取 TGT
        String tgt = tgtServer.getTGT(username);
        System.out.println("TGT：" + tgt);

        if (tgt == null ){
            return new ResponseEntity("用户名错误。", HttpStatus.BAD_REQUEST);
        }

        // 3、设置cookie（1小时）
        Cookie cookie = new Cookie("usertgt", username + "@" + tgt);
        cookie.setMaxAge(1 * 60 * 60);             // Cookie有效时间 设置Cookie的有效时长（1小时）
        cookie.setPath("/");                       // Cookie有效路径
        cookie.setHttpOnly(true);                  // 只允许服务器获取cookie
        response.addCookie(cookie);

        return "tgt:" + tgt;
    }

*/



}