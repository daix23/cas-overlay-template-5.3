package com.zjasm.controller;


import com.zjasm.captcha.CaptchaUtil;
import com.zjasm.util.Dbutil;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jdbc.QueryJdbcAuthenticationProperties;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

@RestController
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RegController {

    @Autowired
    private CasConfigurationProperties casProperties;

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

        CaptchaUtil captchaUtil = new CaptchaUtil(95, 40, 4);
        session.setAttribute("captcha", captchaUtil.getCode());
        captchaUtil.write(response.getOutputStream());
        return null;
    }


    @GetMapping(value = "getServices")
    public String getServicesByUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        Object usernameObj = session.getAttribute("username");
        String username = "";
        if(usernameObj!=null){
            username = usernameObj.toString();
        }
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        DriverManagerDataSource d=new DriverManagerDataSource();
        List<QueryJdbcAuthenticationProperties> jdbcProsList = casProperties.getAuthn().getJdbc().getQuery();
        QueryJdbcAuthenticationProperties jdbcPros = jdbcProsList.get(0);
        QueryJdbcAuthenticationProperties jdbcPros1 = jdbcProsList.get(1);
        d.setDriverClassName(jdbcPros.getDriverClass());
        d.setUrl(jdbcPros.getUrl());
        d.setUsername(jdbcPros.getUser());
        d.setPassword(jdbcPros.getPassword());
        JdbcTemplate template=new JdbcTemplate();
        template.setDataSource(d);
        final List listSiteDomain = template.queryForList(jdbcPros1.getSql(),username);
        HashMap<String, Object> jsonObj = new HashMap<String, Object>();
        jsonObj.put("items", listSiteDomain);
        JSONObject json =  new JSONObject(jsonObj);
        out.write(json.toString());
        out.flush();
        out.close();
        return null;
    }

}
