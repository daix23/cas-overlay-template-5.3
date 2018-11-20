package com.zjasm.util;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jdbc.QueryJdbcAuthenticationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

public class Dbutil {
    private static JdbcTemplate template = null;

    /**
     * 获取唯一实例
     * @return
     */
    public static JdbcTemplate getInstance(){
        if(template==null){
            synchronized (Dbutil.class){
                if(template==null){
                    template = newJdbcT();
                }
            }
        }
        return template;
    }


    /**
     * 构造方法
     */
    private static  JdbcTemplate newJdbcT(){
        template=new JdbcTemplate();
        try {
            DriverManagerDataSource d=new DriverManagerDataSource();
            AppliProUtil plu = AppliProUtil.getInstance();
            d.setDriverClassName(plu.getOneProp("cas.authn.jdbc.query[0].driverClass"));
            d.setUrl(plu.getOneProp("cas.authn.jdbc.query[0].url"));
            d.setUsername(plu.getOneProp("cas.authn.jdbc.query[0].user"));
            d.setPassword(plu.getOneProp("cas.authn.jdbc.query[0].password"));
            template.setDataSource(d);
        } catch (Exception ex) {
            throw new RuntimeException(ex+"数据库连接失败！");
        }
        return template;
    }

}
