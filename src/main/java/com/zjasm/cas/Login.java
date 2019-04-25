package com.zjasm.cas;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.commnetsoft.IDMClient;
import com.commnetsoft.IDMConfig;
import com.commnetsoft.Prop;
import com.commnetsoft.model.GetUserInfoResult;
import com.commnetsoft.model.IdValidationResult;
import com.commnetsoft.model.TicketValidationResult;
import com.commnetsoft.proxy.SsoClient;
import com.commnetsoft.proxy.model.CallResult;
import com.commnetsoft.proxy.model.UserInfo;
import com.commnetsoft.util.ParameterUtil;
import com.commnetsoft.util.StrHelper;
import com.zjasm.captcha.UsernamePasswordCaptchaCredential;
import com.zjasm.exception.*;
import com.zjasm.model.Useryw;
import com.zjasm.util.*;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.GeneralSecurityException;
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
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        AuthenticationHandlerExecutionResult handlerResult = null;
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = attributes.getRequest().getSession();
        String username=mycredential1.getUsername();
        session.setAttribute("username", username);
        //读取config.properties配置
        PropertiesLoaderUtil propertiesLoaderUtil = PropertiesLoaderUtil.getInstance();
        /*String hidTicket = mycredential1.getHidTicket();
        if(!hidTicket.isEmpty()){//三方登录ticket传入登录
            String hidLogintype = mycredential1.getHidLogintype();
            if(hidLogintype!=null&&"gryh".equals(hidLogintype)){
                SsoClient client = SsoClient.getInstance();//单点登录工具
                //登录认证
                CallResult cr = client.login(request, hidTicket);
                logger.info("单点登录，错误码："+cr.getResult()+"，错误信息："+cr.getErrmsg()+"。 ");
                if("0".equals(cr.getResult())){//认证成功登录系统
                    UserInfo user = client.getUser(request);
                    String username = user.getUsername();
                    logger.info("获取用户信息，错误码："+user.getResult()+"，错误信息："+user.getErrmsg()+"。用户信息 "+username);
                    if("0".equals(user.getResult())){
                        //TODO 获取用户信息成功， 相关业务
                        //1、判断是否存在
                        String ywRestURL = propertiesLoaderUtil.getOneProp("ywRestURL");
                        String userResult =HttpRequestUtil.sendGet(ywRestURL+"/Personal/User/verifyUser", "loginName="+username, "utf-8");
                        JSONObject m1= JSON.parseObject(userResult);//将json文本转化为jsonobject
                        int m1code = (Integer)m1.get("code");
                        if(m1code==0){//未授权
                            //2、入库
                            //重新组装实体类
                            Useryw useryw = installUser(user);
                            String userStr = JSON.toJSONString(useryw);
                            JSONObject res = HttpPostJsonUtil.doPost(ywRestURL+"/Personal/User/addUser",userStr);
                            int code = (Integer)res.get("code");
                            if(code==1){
                                logger.info("授权用户信息入库成功！");
                            }else{
                                logger.info("授权用户信息入库失败！"+res.get("message"));
                            }
                        }
                        //3、登录信息返回
                        handlerResult = authOkResultPerson(attributes,username,user,mycredential1);
                    }
                }else{//认证失败
                    logger.info("单点登录失败，错误码："+cr.getResult()+"，错误信息："+cr.getErrmsg()+"。 ");
                }
            }else if(hidLogintype!=null&&"dwyh".equals(hidLogintype)){
                IdmConfigUtil.getInstance();
                Prop prop = IDMConfig.getProp(IdmConfigUtil.IDM_KEY_GOV);
                IDMClient client = new IDMClient(prop);
                //调用IDMAuth的票据认证方法,返回认证结果
                TicketValidationResult ticketValidation = client.ticketValidation(request, hidTicket);
                if (IDMClient.SUCCESS.equals(ticketValidation.getResult())) {
                    //认证成功
                    GetUserInfoResult getUserInfoResult =  client.getUserInfo(request);
                    Map<String, Object> user = getUserInfoResult.getUser();
                    String idmToken = ticketValidation.getToken();
                    HttpSession session = attributes.getRequest().getSession();
                    session.setAttribute("idmToken", idmToken);
                    String username = (String)user.get("username");
                    String orgcode =  (String)user.get("orgcode");
                    handlerResult = authOkResult(attributes,username,user,mycredential1,orgcode);
                } else {
                    throw new LoginException();
                }
            }else{
                throw new LoginException();
            }
        }else{//自己页面登录*/
            String pwdRSA = mycredential1.getPassword();
            // 服务器端，使用RSAUtils工具类对密文进行解密
            String pwdStr = RSAUtils.decryptStringByJs(pwdRSA);
            String captcha = mycredential1.getCaptcha();
            String orgcode = mycredential1.getOrgcode();
            String devcoding = mycredential1.getDevcoding();//组织域名
            String logintype = mycredential1.getLogintype();//登录类型
            username = username+"."+devcoding;
            /*String right = attributes.getRequest().getSession().getAttribute("captcha").toString();
            if(!captcha.equalsIgnoreCase(right)){
                throw new InvalidCaptchaException("验证码错误！");
            }*/
            //自定义操作库
            JdbcTemplate template = Dbutil.getInstance();

            if("grlogin".equals(logintype)){//个人登录
                SsoClient client = SsoClient.getInstance();//单点登录工具
                CallResult callResult= client.login(request,username,null,pwdStr);
                logger.info("个人单点登录，错误码："+callResult.getResult()+"，错误信息："+callResult.getErrmsg()+"。 ");
                if("0".equals(callResult.getResult())){//认证成功登录系统
                    UserInfo user = client.getUser(request);
                    logger.info("获取用户信息，错误码："+user.getResult()+"，错误信息："+user.getErrmsg()+"。用户信息 "+user.getUsername());
                    if("0".equals(user.getResult())){
                        //TODO 获取用户信息成功， 相关业务
                        //1、判断是否存在
                        String ywRestURL = propertiesLoaderUtil.getOneProp("ywRestURL");
                        String userResult =HttpRequestUtil.sendGet(ywRestURL+"/Personal/User/verifyUser", "loginName="+username, "utf-8");
                        JSONObject m1= JSON.parseObject(userResult);//将json文本转化为jsonobject
                        int m1code = (Integer)m1.get("code");
                        if(m1code==0){//未授权
                            //2、入库
                            //重新组装实体类
                            Useryw useryw = installUser(user);
                            String userStr = JSON.toJSONString(useryw);
                            JSONObject res = HttpPostJsonUtil.doPost(ywRestURL+"/Personal/User/addUser",userStr);
                            int code = (Integer)res.get("code");
                            if(code==1){
                                logger.info("授权用户信息入库成功！");
                            }else{
                                logger.info("授权用户信息入库失败！"+res.get("message"));
                            }
                        }
                        //3、登录信息返回
                        handlerResult = authOkResultPerson(attributes,username,user,mycredential1);
                    }
                }else{//认证失败
                    LogUtil.LoginOut(request,"LOGIN","0","登录失败，"+callResult.getErrmsg());
                    logger.info("个人单点登录失败，错误码："+callResult.getResult()+"，错误信息："+callResult.getErrmsg()+"。 ");
                    throw new LoginException();
                }
                //throw new NoOpenException("此功能暂未开放！");
            }else{//法人登录
                if(orgcode==null||"".equals(orgcode)){
                    LogUtil.LoginOut(request,"LOGIN","0","登录失败，组织不能为空！");
                    throw new InvalidOrgException("组织不能为空！");
                }
                //查询数据库加密的的密码
                Map<String,Object> user = null;
                try {
                    String userOrgSql = "SELECT u.userid,u.disabled,u.localpwd,u.userarea,u.loginname,org.orgcoding,org.orgname FROM userinfo u, user_org_ref ref,s_orginfo org " +
                            "where  u.userid=ref.userId and  ref.orgid=org.id and (u.loginname='"+username+"' OR u.phone='"+username+"') and org.oid='"+orgcode+"' " +
                            "AND u.deleteflag=0 and ref.isdel=0 and org.isdel=0  order by org.orderby asc LIMIT 1";
                    user = template.queryForMap(userOrgSql);
                }catch (Exception e){
                    LogUtil.LoginOut(request,"LOGIN","0","登录失败，用户不存在！");
                    throw new NoUserException("用户不存在！");
                }
                //获取验证开关
                boolean authIdmFlag = Boolean.parseBoolean(propertiesLoaderUtil.getOneProp("authIdmFlag"));
                logger.info("易和接入开关authIdmFlag："+authIdmFlag);
                if(authIdmFlag){
                    logger.info("易和用户验证");
                    String nameConcat = username;
                    //String nameConcat = username+"."+devcoding;
                    logger.info("易和接入nameConcat："+nameConcat);
                    IdmConfigUtil.getInstance();
                    Prop prop = IDMConfig.getProp(IdmConfigUtil.IDM_KEY_GOV);
                    logger.info("易和接入prop："+prop.getUrl());
                    IDMClient client = new IDMClient(prop);
                    IdValidationResult idValiResult=client.idValidation(attributes.getRequest(), nameConcat, pwdStr);
                    logger.info("易和接入idValiResult："+idValiResult.getResult());
                    if (IDMClient.SUCCESS.equals(idValiResult.getResult())) {
                        logger.info("易和用户登录成功");
                        String idmToken = idValiResult.getToken();
                        session.setAttribute("idmToken", idmToken);
                        handlerResult = authOkResult(attributes,username,user,mycredential1,orgcode );
                    }else {
                        String errmsg = idValiResult.getErrmsg();
                        LogUtil.LoginOut(request,"LOGIN","0","登录失败，"+errmsg);
                        logger.info("易和用户登录失败："+errmsg);
                        throw new NoAuthException(errmsg);
                    }
                }else{//本地验证
                    //给数据进行md5加密
                    logger.info("本地用户验证");
                    String pwd = new CustomPasswordEncoder().encode(pwdStr);
                    if(pwd.equals(user.get("localpwd").toString())){
                        handlerResult = authOkResult(attributes,username,user,mycredential1,orgcode );
                    }else{
                        LogUtil.LoginOut(request,"LOGIN","0","登录失败，密码错误！");
                        throw new InvalidPasswordException("密码错误！");
                    }
                }
            }
        //}
        LogUtil.LoginOut(request,"LOGIN","1","登录成功");
        return handlerResult;
    }


    private Useryw installUser(UserInfo user){
        Useryw useryw = new Useryw();
        useryw.setUserid(user.getUserid());
        useryw.setAuthlevel(user.getAuthlevel());
        useryw.setIdtype(user.getIdtype());
        useryw.setUsername(user.getUsername());
        useryw.setIdnum(user.getIdnum());
        useryw.setPassport(user.getPassport());
        useryw.setPermitlicense(user.getPermitlicense());
        useryw.setTaiwanlicense(null);
        useryw.setOfficerlicense(user.getOfficerlicense());
        useryw.setGreencard(null);
        useryw.setSex(user.getSex());
        useryw.setNation(user.getNation());
        useryw.setLoginname(user.getLoginname());
        useryw.setEmail(user.getEmail());
        useryw.setMobile(user.getMobile());
        useryw.setPostcode(user.getPostcode());
        useryw.setCakey(user.getCakey());
        useryw.setBirthday(user.getBirthday());
        useryw.setCountry(user.getCountry());
        useryw.setProvince(user.getProvince());
        useryw.setCity(user.getCity());
        useryw.setOfficeaddress(user.getOfficeaddress());
        useryw.setOfficephone(user.getOfficephone());
        useryw.setOfficefax(user.getOfficefax());
        useryw.setHomephone(user.getHomephone());
        useryw.setHomeaddress(user.getHomeaddress());
        useryw.setUseable(user.getUseable());
        useryw.setOrderby(user.getOrderby());
        useryw.setHeadpicture(null);
        return useryw;

    }

    /**
     * 个人用户多属性返回
     * @param attributes
     * @param username
     * @param user
     * @param mycredential1
     * @return
     */
    public AuthenticationHandlerExecutionResult authOkResultPerson(ServletRequestAttributes attributes,
                                                             String username,UserInfo user,
                                                             UsernamePasswordCaptchaCredential mycredential1){
        HttpSession session = attributes.getRequest().getSession();
        session.setAttribute("username", username);
        //存放数据到里面
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("userid", user.getMobile());
        result.put("loginname", user.getLoginname());
        result.put("mobile", user.getMobile());
        result.put("username", user.getUsername());
        result.put("idnum", user.getIdnum());
        PropertiesLoaderUtil propertiesLoaderUtil = PropertiesLoaderUtil.getInstance();
        String authsysPerson = propertiesLoaderUtil.getOneProp("authsysPerson");
        String[] strArr = authsysPerson.split(",");
        if(strArr!=null&&strArr.length!=0){
            List syss = new ArrayList();
            for(int i=0;i<strArr.length;i++){
                String one = strArr[i];
                syss.add(one);
            }
            result.put("authsys_multi", syss);
        }
        return createHandlerResult(mycredential1, this.principalFactory.createPrincipal(username, result));
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