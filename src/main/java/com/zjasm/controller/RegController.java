package com.zjasm.controller;


import com.commnetsoft.IDMClient;
import com.commnetsoft.IDMConfig;
import com.commnetsoft.Prop;
import com.commnetsoft.model.GetUserInfoResult;
import com.commnetsoft.model.TicketValidationResult;
import com.commnetsoft.util.ParameterUtil;
import com.commnetsoft.util.StrHelper;
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
import java.util.Map;

@RestController
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RegController {

    @Autowired
    private CasConfigurationProperties casProperties;


    public final static String IDM_KEY_GOV = "idm_gov";


    @GetMapping("test")
    public String test(){
        return "Hello World!";
    }


    @GetMapping(value = "captcha")
    public String capcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 设置响应的类型格式为图片格式
        response.setContentType("image/png");
        //禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession();

        CaptchaUtil captchaUtil = new CaptchaUtil(95, 38, 4);
        session.setAttribute("captcha", captchaUtil.getCode());
        captchaUtil.write(response.getOutputStream());
        return null;
    }


    /**
     * 票据认证登录接口
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping(value = "ticketAuth")
    public String ticketAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
        /**
         * 票据登录总入口。
         * 接受参数ticket和sp参数
         * @param ticket 票据，一次性使用，1分钟内使用。过期或者使用后失效。请在1分钟内到IDM认证
         * @param sp 跳转业务页面。 如果为空，则跳转到主页面，不为空则跳转到该页面地址。
         */
        /**
         * 第一步：实例化一个客户端对象
         */
        Prop prop = IDMConfig.getProp(IDM_KEY_GOV);
        IDMClient client = new IDMClient(prop);
        /**
         * 收到票据ticket，进行票据认证。
         */
        String ticket = request.getParameter("ticket");
        //调用IDMAuth的票据认证方法,返回认证结果
        TicketValidationResult ticketValidation = client.ticketValidation(request, ticket);
        if (IDMClient.SUCCESS.equals(ticketValidation.getResult())) {
            //认证成功
            String sp = request.getParameter("sp");
            if(StrHelper.isEmpty(sp)){
                //TODO 没有sp参数跳转到默认页面。
                /**
                 * 注意：一定要重定向，防止用户刷新页面再次票据认证。
                 * 因为票据只能使用一次，而导致刷新认证失败问题
                 */
                GetUserInfoResult getUserInfoResult =  client.getUserInfo(request);
                Map<String, Object> user = getUserInfoResult.getUser();
                ParameterUtil.renderHtml(response, "单点登录成功uid="+user.get("uid")+", username="+user.get("username"));
                //resp.sendRedirect("");
            }else{
                //TODO 有sp参数跳转到具体业务页面
                response.sendRedirect(sp);
            }

        } else {
            //TODO 认证失败,这里可以重定向至登录界面
            ParameterUtil.renderHtml(response, "票据认证失败=ticket"+ticket);
        }
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
