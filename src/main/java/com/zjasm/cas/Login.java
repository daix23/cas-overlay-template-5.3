package com.zjasm.cas;

import com.commnetsoft.IDMClient;
import com.commnetsoft.IDMConfig;
import com.commnetsoft.Prop;
import com.commnetsoft.model.IdValidationResult;
import com.zjasm.captcha.UsernamePasswordCaptchaCredential;
import com.zjasm.exception.InvalidCaptchaException;
import com.zjasm.exception.InvalidPasswordException;
import com.zjasm.exception.NoAuthException;
import com.zjasm.exception.NoUserException;
import com.zjasm.util.Dbutil;
import com.zjasm.util.IdmConfigUtil;
import com.zjasm.util.PropertiesLoaderUtil;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.JdbcPrincipalAttributesProperties;
import org.apereo.cas.configuration.model.support.jdbc.QueryJdbcAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableConfigurationProperties(CasConfigurationProperties.class)
public class Login extends AbstractPreAndPostProcessingAuthenticationHandler {

    private static final Logger logger = LoggerFactory.getLogger(Login.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    public Login(String name, ServicesManager servicesManager, PrincipalFactory principalFactory, Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(Credential credential) throws GeneralSecurityException, PreventedException {
        UsernamePasswordCaptchaCredential mycredential1 = (UsernamePasswordCaptchaCredential) credential;
        AuthenticationHandlerExecutionResult handlerResult = null;
        String captcha = mycredential1.getCaptcha();
        String orgcode = mycredential1.getOrgcode();
        String devcoding = mycredential1.getDevcoding();//组织域名
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String right = attributes.getRequest().getSession().getAttribute("captcha").toString();
        //跨域问题
        /*HttpServletResponse response = attributes.getResponse();
        response.setHeader("Access-Control-Allow-Origin", "*"); //解决跨域访问报错
        response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600"); //设置过期时间
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, client_id, uuid, Authorization");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // 支持HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // 支持HTTP 1.0. response.setHeader("Expires", "0");*/

        if(!captcha.equalsIgnoreCase(right)){
            throw new InvalidCaptchaException("验证码错误！");
        }

        List<QueryJdbcAuthenticationProperties> jdbcProsList = casProperties.getAuthn().getJdbc().getQuery();
        QueryJdbcAuthenticationProperties jdbcPros = jdbcProsList.get(0);
        //QueryJdbcAuthenticationProperties jdbcPros1 = jdbcProsList.get(1);
        //自定义操作库
        JdbcTemplate template = Dbutil.getInstance();
        //获取服务的子系统链接service
        String serviceStr = attributes.getRequest().getParameter("service");
        //判断用户是否属于该系统
        /*if(serviceStr!=""&&serviceStr!=null){
            final List listSiteDomain = template.queryForList(jdbcPros1.getSql(),mycredential1.getUsername());
            if(listSiteDomain!=null&&!listSiteDomain.isEmpty()){
                for(int i=0;i<listSiteDomain.size();i++) {
                    String site = ((LinkedCaseInsensitiveMap)listSiteDomain.get(i)).get("site_domain").toString();
                    if(site!=null&&serviceStr.indexOf(site)!=-1){
                        break;
                    }
                    if(i==listSiteDomain.size()-1){
                        throw new NoAuthException("无权限访问！");
                    }
                }
            }else {
                throw new NoAuthException("无权限访问！");
            }
        }*/


        String username=mycredential1.getUsername();
        String pwdStr = mycredential1.getPassword();

        //查询数据库加密的的密码
        Map<String,Object> user = null;
        try {
            String userOrgSql = "SELECT u.userid,u.disabled,u.localpwd,u.userarea,u.loginname,org.orgcoding,org.orgname FROM userinfo u, user_org_ref ref,s_orginfo org " +
                    "where  u.userid=ref.userId and  ref.orgid=org.id and (u.loginname='"+username+"' OR u.phone='"+username+"') and org.orgcoding='"+orgcode+"' " +
                    "AND u.deleteflag=0 and ref.isdel=0 and org.isdel=0  order by org.orgcoding asc LIMIT 1";

            //user = template.queryForMap(jdbcPros.getSql(), username);
            user = template.queryForMap(userOrgSql);
        }catch (Exception e){
            throw new NoUserException("用户不存在！");
        }
        //获取验证开关
        PropertiesLoaderUtil propertiesLoaderUtil = PropertiesLoaderUtil.getInstance();
        boolean authIdmFlag = Boolean.parseBoolean(propertiesLoaderUtil.getOneProp("authIdmFlag"));
        logger.info("易和接入开关authIdmFlag："+authIdmFlag);
        if(authIdmFlag){
            logger.info("易和用户验证");
            //1、对接易和用户名密码验证并返回令牌
            /*String resultStr = "";
            try {
                PortTypeParams params = new PortTypeParams();
                SimpleAuthService service = new SimpleAuthService();
                SimpleAuthServicePortType portType = service
                        .getSimpleAuthServiceHttpPort();
                resultStr = portType.idValidation(params.getServiceCode(),
                        params.getCurTimeStr(), params.getSign(), username,
                        orgcode, params.getEncryptiontype(), pwdStr,
                        params.getDatatype());
                if("".equals(resultStr)){
                    String msg = "单点登录失败易和验证失败：易和验证失败，请联系管理员！";
                    throw new NoAuthException(msg);
                }else{
                    JSONObject verifyResult = JSON.parseObject(resultStr);
                    if (verifyResult.getString("result").equals("0")) {
                        //易和验证成功
                    }else{
                        throw new NoAuthException("msg");
                    }
                }*/
            //不需要解码
            //String pswBs64= CommonUtil.getFromBASE64(pwdStr);

            //--3个参数，1、sql 2、要传递的参数数组 3、返回来的对象class
            //获取组合name
            /*String sqlStr = "SELECT CONCAT(LOGINNAME,'.',devcoding) AS LOGINNAME FROM userinfo WHERE Userorg like '"+orgcode+"%' AND " +
                    "LOGINNAME='"+username+"' AND deleteflag=0 AND usertype=1";
            String nameConcat = (String) template.queryForObject(sqlStr,java.lang.String.class);*/
            //String nameConcat = (String) template.queryForObject(jdbcPros2.getSql(),new Object[] {orgcode,username},java.lang.String.class);
            //String nameConcat = username+".hz";
            String nameConcat = username+"."+devcoding;
            logger.info("易和接入nameConcat："+nameConcat);
            IdmConfigUtil.getInstance();
            Prop prop = IDMConfig.getProp(IdmConfigUtil.IDM_KEY_GOV);
            logger.info("易和接入prop："+prop.getUrl());
            IDMClient client = new IDMClient(prop);
            IdValidationResult idValiResult=client.idValidation(attributes.getRequest(), nameConcat, pwdStr);
            logger.info("易和接入idValiResult："+idValiResult.getResult());
            if (IDMClient.SUCCESS.equals(idValiResult.getResult())) {
                logger.info("易和用户登录成功");
                //重置本地密码，本地单点登录
                //String localpwd = (String)user.get("localpwd");
                //String lp = MD5Util2.convertMD5(localpwd);
                mycredential1.setPassword("123456");
                String idmToken = idValiResult.getToken();
                HttpSession session = attributes.getRequest().getSession();
                session.setAttribute("idmToken", idmToken);
                handlerResult = authOkResult(attributes,username,user,mycredential1,orgcode );
            }else {
                String errmsg = idValiResult.getErrmsg();
                logger.info("易和用户登录失败："+errmsg);
                throw new NoAuthException(errmsg);
            }
        }else{//本地验证
            //给数据进行md5加密
            logger.info("本地用户验证");
            String pwd = new CustomPasswordEncoder().encode(pwdStr);
            //BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            //if(encoder.matches(pwd,user.get("localpwd").toString())){
            if(pwd.equals(user.get("localpwd").toString())){
                handlerResult = authOkResult(attributes,username,user,mycredential1,orgcode );
            }else{
                throw new InvalidPasswordException("密码错误！");
            }
        }
        return handlerResult;
    }

    /**
     * 多属性返回
     * @param attributes
     * @param username
     * @param user
     * @param mycredential1
     * @param orgcode
     * @return
     */
    public AuthenticationHandlerExecutionResult authOkResult(ServletRequestAttributes attributes,
                                                             String username,Map<String,Object> user,
                                                             UsernamePasswordCaptchaCredential mycredential1,String orgcode ){
        HttpSession session = attributes.getRequest().getSession();
        session.setAttribute("username", username);
        //返回多属性
        /*JdbcPrincipalAttributesProperties jdbcAttrs = casProperties.getAuthn().getAttributeRepository().getJdbc().get(0);
        Map<String, Object> map=new HashMap<>();
        Map<String, String> attrMap =  jdbcAttrs.getAttributes();
        for (String str : attrMap.keySet()) {
            //map.keySet()返回的是所有key的值
            String val = (String)attrMap.get(str);//得到每个key多对用value的值
            map.put(val, user.get(val).toString());
        }
        //多返回一个orgcode参数
        map.put("orgcode", orgcode);
        return createHandlerResult(mycredential1, principalFactory.createPrincipal(username, map), null);*/
        //存放数据到里面
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("userid", user.get("userid"));
        result.put("loginname", username);
        result.put("userarea", user.get("userarea"));
        result.put("orgcode", user.get("orgcoding"));//读取返回的组织信息
        result.put("orgname", user.get("orgname"));//读取返回的组织信息
        //返回用户关联角色信息
        //自定义操作库
        JdbcTemplate template = Dbutil.getInstance();
        String userRoleSysSql = "select * from v_user_role_authsys where username='"+username+"'";
        List list  = template.queryForList(userRoleSysSql);
        if(list!=null&&!list.isEmpty()){
            List roles = new ArrayList();
            List syss = new ArrayList();
            for(int i=0;i<list.size();i++){
                LinkedCaseInsensitiveMap  obj = (LinkedCaseInsensitiveMap ) list.get(i);
                if("role".equals(obj.get("ATTR_KEY"))){
                    roles.add(obj.get("ATTR_VAL"));
                }else if("authsys".equals(obj.get("ATTR_KEY"))){
                    syss.add(obj.get("ATTR_VAL"));
                }
            }
            result.put("role_multi", roles);
            result.put("authsys_multi", syss);
        }
        //允许登录，并且通过this.principalFactory.createPrincipal来返回用户属性
        return createHandlerResult(mycredential1, this.principalFactory.createPrincipal(username, result));
        //return createHandlerResult(mycredential1, principalFactory.createPrincipal(username), null);
    }


    @Override
    public boolean supports(Credential credential) {
        return credential instanceof UsernamePasswordCaptchaCredential;
    }
}