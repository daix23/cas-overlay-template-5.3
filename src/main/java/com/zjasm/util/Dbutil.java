package com.zjasm.util;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jdbc.QueryJdbcAuthenticationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;


@EnableConfigurationProperties(CasConfigurationProperties.class)
public class Dbutil {

    private Dbutil(){}

    @Autowired
    private static CasConfigurationProperties casProperties;

    /*
     * 定义静态方法，返回数据库的连接对象
     */
    public static JdbcTemplate getJdbcTemplate(){
        JdbcTemplate template = null;
        try {
            DriverManagerDataSource d=new DriverManagerDataSource();
            List<QueryJdbcAuthenticationProperties> jdbcProsList = casProperties.getAuthn().getJdbc().getQuery();
            QueryJdbcAuthenticationProperties jdbcPros = jdbcProsList.get(0);
            d.setDriverClassName(jdbcPros.getDriverClass());
            d.setUrl(jdbcPros.getUrl());
            d.setUsername(jdbcPros.getUser());
            d.setPassword(jdbcPros.getPassword());
            template=new JdbcTemplate();
            template.setDataSource(d);
        } catch (Exception ex) {
            throw new RuntimeException(ex+"数据库连接失败！");
        }
        return template;
    }


}
