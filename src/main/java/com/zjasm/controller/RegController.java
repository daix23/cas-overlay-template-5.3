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
import com.zjasm.exception.NoAuthException;
import com.zjasm.model.ReturnMessage;
import com.zjasm.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jdbc.QueryJdbcAuthenticationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RegController {

    private static final Logger logger = LoggerFactory.getLogger(RegController.class);

    @Autowired
    private CasConfigurationProperties casProperties;


    public final static String IDM_KEY_GOV = "idm_gov";


    /*@GetMapping("test")
    public String test(HttpServletRequest request, HttpServletResponse response){
        HttpSession session = request.getSession();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return "Hello World!";
    }*/


    @GetMapping("noauth")
    public void test(HttpServletRequest request, HttpServletResponse response) throws Exception {
        throw new NoAuthException("无权限访问！");
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

    @GetMapping(value = "regPwdUrl")
    public void regPwdUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        //获取验证开关
        PropertiesLoaderUtil propertiesLoaderUtil = PropertiesLoaderUtil.getInstance();
        boolean authIdmFlag = Boolean.parseBoolean(propertiesLoaderUtil.getOneProp("authIdmFlag"));
        HashMap<String, Object> jsonObj = new HashMap<String, Object>();
        String regUrl = "#";
        String pwdUrl = "#";
        if(authIdmFlag){//易和
            regUrl = propertiesLoaderUtil.getOneProp("registeruserUrlIdm");
            pwdUrl = propertiesLoaderUtil.getOneProp("forgotpwdUrlIdm");
        }else{
            regUrl = propertiesLoaderUtil.getOneProp("registeruserUrl");
            pwdUrl = propertiesLoaderUtil.getOneProp("forgotpwdUrl");
        }
        jsonObj.put("regUrl", regUrl);
        jsonObj.put("pwdUrl", pwdUrl);
        JSONObject json =  new JSONObject(jsonObj);
        out.write(json.toString());
        out.flush();
        out.close();
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

        List<QueryJdbcAuthenticationProperties> jdbcProsList = casProperties.getAuthn().getJdbc().getQuery();
        QueryJdbcAuthenticationProperties jdbcPros1 = jdbcProsList.get(1);
        //自定义操作库
        JdbcTemplate template = Dbutil.getInstance();
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
        //自定义操作库
        JdbcTemplate template = Dbutil.getInstance();
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
            if(service==null){
                response.sendRedirect("logout");
            }else{
                response.sendRedirect("logout?service="+service);
            }
        }
    }

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    /**
     * 注册service
     * @param serviceId 服务名
     * @return
     */
    @GetMapping(value = "addService")
    public Object addService(HttpServletRequest request, HttpServletResponse response) {
        String serviceId = request.getParameter("serviceId");
        ReturnMessage returnMessage = new ReturnMessage();
        //通过请求ip安全防护接口
        boolean flag = ipCheck(request);
        if(flag){
            if(serviceId==null){
                logger.error("注册service异常,serviceId不能为空");
                returnMessage.setCode(501);
                returnMessage.setMessage("添加失败,serviceId不能为空");
            }else{
                try {
                    String ss="^(https|imaps|http)://"+serviceId+".*";
                    //判断是否重复
                    JdbcTemplate template = Dbutil.getInstance();
                    String sql = "select count(1) from regexregisteredservice where serviceId='"+ss+"'";
                    int ii= template.queryForObject(sql,Integer.class);
                    if(ii>0){
                        logger.error("注册service异常,serviceId已存在");
                        returnMessage.setCode(503);
                        returnMessage.setMessage("添加失败,serviceId已存在");
                    }else{
                        RegexRegisteredService service = new RegexRegisteredService();
                        ReturnAllAttributeReleasePolicy re = new ReturnAllAttributeReleasePolicy();
                        //服务访问控制https://apereo.github.io/cas/5.3.x/installation/Configuring-Service-Access-Strategy.html
                        DefaultRegisteredServiceAccessStrategy drsas = new DefaultRegisteredServiceAccessStrategy();
                        drsas.setEnabled(true);
                        drsas.setSsoEnabled(true);
                        AppliProUtil appliProUtil = AppliProUtil.getInstance();
                        String sname = appliProUtil.getOneProp("cas.server.name");
                        String sprefix = appliProUtil.getOneProp("cas.server.prefix");
                        String reUrl = sprefix.replace("${cas.server.name}",sname)+"/noauth";
                        drsas.setUnauthorizedRedirectUrl(new URI(reUrl));
                        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
                        Set<String> setStr = new HashSet<String>();
                        setStr.add("http://"+serviceId);
                        map.put("authsys_multi",setStr);
                        drsas.setRequiredAttributes(map);
                        service.setAccessStrategy(drsas);

                        service.setServiceId(ss);
                        service.setEvaluationOrder(1);
                        service.setTheme("apereo");
                        service.setAttributeReleasePolicy(re);
                        service.setName(serviceId);
                        //这个是为了单点登出而作用的
                        //service.setLogoutUrl(new URL("http://"+serviceId));
                        servicesManager.save(service);
                        //执行load让他生效
                        servicesManager.load();
                        returnMessage.setCode(200);
                        returnMessage.setMessage("添加成功");
                    }
                } catch (Exception e) {
                    logger.error("注册service异常",e);
                    returnMessage.setCode(500);
                    returnMessage.setMessage("添加失败,注册service异常");
                }
            }
        }else{
            logger.error("注册service异常,非法请求");
            returnMessage.setCode(502);
            returnMessage.setMessage("添加失败,非法请求");
        }
        return returnMessage;
    }

    /**
     * 删除service异常
     * @param serviceId
     * @return
     */
    @GetMapping(value = "delService")
    public Object delService(HttpServletRequest request, HttpServletResponse response) {
        String serviceId = request.getParameter("serviceId");
        ReturnMessage returnMessage = new ReturnMessage();
        //通过请求ip安全防护接口
        boolean flag = ipCheck(request);
        if(flag){
            if(serviceId==null){
                logger.error("删除service异常,serviceId不能为空");
                returnMessage.setCode(501);
                returnMessage.setMessage("删除失败,serviceId不能为空");
            }else{
                boolean sqlFlag = com.zjasm.util.ParameterUtil.sqlInj(serviceId);
                if(!sqlFlag){
                    try {
                        String ss="^(https|imaps|http)://"+serviceId+".*";
                        //RegisteredService service = servicesManager.findServiceBy(a);
                        //servicesManager.delete(service);//java.lang.IllegalArgumentException: ‘actionPerformed’ cannot be null.
                        //自定义操作库删除
                        JdbcTemplate template = Dbutil.getInstance();
                        String sql = "delete from regexregisteredservice where serviceId='"+ss+"'";
                        int ii= template.update(sql);
                        //执行load生效
                        servicesManager.load();
                        if(ii>=1){
                            returnMessage.setCode(200);
                            returnMessage.setMessage("删除成功");
                        }else{
                            logger.error("删除service异常,未找到此service");
                            returnMessage.setCode(404);
                            returnMessage.setMessage("删除失败,未找到此service");
                        }
                    } catch (Exception e) {
                        logger.error("删除service异常",e);
                        returnMessage.setCode(500);
                        returnMessage.setMessage("删除失败,删除service异常");
                    }
                }else{
                    logger.error("删除service异常,非法请求");
                    returnMessage.setCode(502);
                    returnMessage.setMessage("删除失败,非法请求");
                }
            }
        }else{
            logger.error("删除service异常,非法请求");
            returnMessage.setCode(502);
            returnMessage.setMessage("删除失败,非法请求");
        }
        return returnMessage;
    }


    /**
     * 请求来源判断
     * @param request
     * @return
     */
    private boolean ipCheck(HttpServletRequest request){
        boolean flag = false;
        PropertiesLoaderUtil pro = PropertiesLoaderUtil.getInstance();
        boolean serFlag = Boolean.parseBoolean(pro.getOneProp("serviceYWFlag"));
        if(serFlag){
            //getRemoteAddr方法返回发出请求的客户机的IP地址。
            //getLocalAddr方法返回WEB服务器的IP地址。
            String remoteAddr = request.getRemoteAddr();//得到来访者的IP地址
            //String localAddr = request.getLocalAddr();//获取WEB服务器的IP地址
            logger.info("request来访者IP地址："+remoteAddr);
            String ip = pro.getOneProp("serviceYWIP");
            logger.info("配置的运维服务器IP地址："+ip);
            if(remoteAddr.equals(ip)){
                flag = true;
            }
        }else{
            flag = true;
        }
        return flag;
    }

}
