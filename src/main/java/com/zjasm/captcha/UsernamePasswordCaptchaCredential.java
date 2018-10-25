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


}