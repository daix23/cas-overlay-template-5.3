package com.zjasm.captcha;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apereo.cas.authentication.UsernamePasswordCredential;

import javax.validation.constraints.Size;

public class UsernamePasswordCaptchaCredential extends UsernamePasswordCredential{


    @Size(min = 4,max = 4, message = "require captcha")
    private String captcha;

    public String getCaptcha() {
        return captcha;
    }

    public UsernamePasswordCaptchaCredential setCaptcha(String captcha) {
        this.captcha = captcha;
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.captcha)
                .toHashCode();
    }
}