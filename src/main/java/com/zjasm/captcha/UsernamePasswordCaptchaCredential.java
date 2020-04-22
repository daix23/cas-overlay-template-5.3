package com.zjasm.captcha;

import org.apereo.cas.authentication.RememberMeUsernamePasswordCredential;

import javax.validation.constraints.Size;

public class UsernamePasswordCaptchaCredential extends RememberMeUsernamePasswordCredential {


    @Size(min = 4, max = 4, message = "验证码不能为空")
    private String captcha;

    public String getCaptcha() {
        return captcha;
    }


    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    @Size(min = 0, max = 32, message = "组织不能为空")
    private String orgcode;

    public String getOrgcode() {
        return orgcode;
    }

    public void setOrgcode(String orgcode) {
        this.orgcode = orgcode;
    }

    //组织域名 (如:公安厅gat.zj)
    private String devcoding;

    public String getDevcoding() {
        return devcoding;
    }

    public void setDevcoding(String devcoding) {
        this.devcoding = devcoding;
    }

    private String logintype;

    public String getLogintype() {
        return logintype;
    }

    public void setLogintype(String logintype) {
        this.logintype = logintype;
    }

    private String hidTicket;

    private String hidLogintype;

    public String getHidTicket() {
        return hidTicket;
    }

    public void setHidTicket(String hidTicket) {
        this.hidTicket = hidTicket;
    }

    public String getHidLogintype() {
        return hidLogintype;
    }

    public void setHidLogintype(String hidLogintype) {
        this.hidLogintype = hidLogintype;
    }
}