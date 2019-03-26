package com.zjasm.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

public class LogUtil {

    public static  void LoginOut(HttpServletRequest request,String oprType,String oprResult,String errorDesc) {
        String remoteAddr = request.getRemoteAddr();//得到来访者的IP地址
        HttpSession session = request.getSession();
        String username = (String)session.getAttribute("username");
        JdbcTemplate template = Dbutil.getInstance();
        String clientType = "WEB";
        java.sql.Timestamp dateTime = new java.sql.Timestamp(new Date().getTime());
        String sql = "insert into log_loginlog_2019(username,ip,optype,clientType,oprTime,oprResult,errorDesc)values(?,?,?,?,?,?,?)";
        Object args[] = {username,remoteAddr,oprType,clientType,dateTime,oprResult,errorDesc};
        int temp = template.update(sql, args);
    }

}
