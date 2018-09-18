package com.zjasm.captcha;

import org.apereo.cas.authentication.RememberMeUsernamePasswordCredential;
import javax.validation.constraints.Size;

public class UsernamePasswordCaptchaCredential extends RememberMeUsernamePasswordCredential {


    @Size(min = 4,max = 4, message = "验证码不能为空")
    private String captcha;

    public String getCaptcha() {
        return captcha;
    }


    public UsernamePasswordCaptchaCredential setCaptcha(String captcha) {
        this.captcha = captcha;
        return this;
    }

}