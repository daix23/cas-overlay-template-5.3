package com.zjasm.controller;


import com.commnetsoft.IDMClient;
import com.commnetsoft.IDMConfig;
import com.commnetsoft.Prop;
import com.commnetsoft.model.GetUserInfoResult;
import com.commnetsoft.model.TicketValidationResult;
import com.commnetsoft.util.ParameterUtil;
import com.commnetsoft.util.ServiceUtil;
import com.commnetsoft.util.StrHelper;
import com.zjasm.captcha.CaptchaUtil;
import com.zjasm.util.CommonUtil;
import com.zjasm.util.HttpRequestUtil;
import com.zjasm.util.IdmConfigUtil;
import com.zjasm.util.PropertiesLoaderUtil;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jdbc.QueryJdbcAuthenticationProperties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(RegController.class);

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
        IdmConfigUtil.getInstance();
        Prop prop = IDMConfig.getProp(IdmConfigUtil.IDM_KEY_GOV);
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

    @GetMapping(value = "getOrg")
    public String getOrg(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String orgcoding = request.getParameter("orgcoding");
        StringBuilder sb=new StringBuilder();
        sb.append("SELECT orgcoding,devcoding,orgname FROM s_orginfo WHERE ISDEL=0 ");
        if (!CommonUtil.isNullOrEmpty(orgcoding)) {
            sb.append(" AND orgcoding LIKE '%"+orgcoding+"%' ");
        }else{
            orgcoding = "";
        }
        String scope = "";
        if (!CommonUtil.isNullOrEmpty(scope) && scope.equals("1")) {
            sb.append("");
        }else{
            sb.append("  AND LENGTH(orgcoding) = LENGTH('"+orgcoding+"') + 3  ");
        }
        sb.append(" ORDER BY orderby ASC");
        String sql = sb.toString();
        DriverManagerDataSource d=new DriverManagerDataSource();
        List<QueryJdbcAuthenticationProperties> jdbcProsList = casProperties.getAuthn().getJdbc().getQuery();
        QueryJdbcAuthenticationProperties jdbcPros = jdbcProsList.get(0);
        d.setDriverClassName(jdbcPros.getDriverClass());
        d.setUrl(jdbcPros.getUrl());
        d.setUsername(jdbcPros.getUser());
        d.setPassword(jdbcPros.getPassword());
        JdbcTemplate template=new JdbcTemplate();
        template.setDataSource(d);
        List list = template.queryForList(sql);
        HashMap<String, Object> jsonObj = new HashMap<String, Object>();
        jsonObj.put("result", list);
        JSONObject json =  new JSONObject(jsonObj);
        out.write(json.toString());
        out.flush();
        out.close();
        return null;
    }

    @GetMapping(value = "/logoutCas")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try{
            logger.info("统一登出入口：logoutCas");
            //获取验证开关
            PropertiesLoaderUtil propertiesLoaderUtil = PropertiesLoaderUtil.getInstance();
            boolean authIdmFlag = Boolean.parseBoolean(propertiesLoaderUtil.getOneProp("authIdmFlag"));
            if(authIdmFlag){//易和登出
                String servicecode = propertiesLoaderUtil.getOneProp("servicecode");
                String servicepwd = propertiesLoaderUtil.getOneProp("servicepwd");
                String idmUrl = propertiesLoaderUtil.getOneProp("idmUrl");
                HttpSession session = request.getSession();
                Object idmTokenObj = session.getAttribute("idmToken");
                String idmToken = null;
                if(idmTokenObj!=null){
                    idmToken = idmTokenObj.toString();
                    /*http   logout 使用令牌实现单点登出IDM。
                    servicecode	资源接入代码
                    time	时间戳，格式为20091010121212
                    sign	数据签名MD5(servicecode+servicepwd+time)
                    token	令牌
                    datatype	数据格式xml/json。默认json，其他错误格式也返回json*/
                    String time = CommonUtil.GetTimeString();
                    String sign = ServiceUtil.getMd5Sing(servicecode, servicepwd, time);
                    String param = "servicecode="+servicecode+"&time="+time+"&sign="+sign+"&token"+idmToken+"&datatype=json";
                    String result = HttpRequestUtil.sendGet(idmUrl+"/logout", param, "utf-8");
                    JSONObject json =  new JSONObject(result);
                    if(IDMClient.SUCCESS.equals(json.getString("result"))){
                        logger.info("易和登出成功");
                    }else{
                        logger.info(json.getString("errmsg"));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            String service = request.getParameter("service");
            response.sendRedirect("logout?service="+service);
        }
    }


}
