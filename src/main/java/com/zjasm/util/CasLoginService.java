package com.zjasm.util;

import com.zjasm.controller.RegController;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CasLoginService {

    private static final Logger Log = LoggerFactory.getLogger(CasLoginService.class);

    public static String getTicket(final String server, final String username, final String password,
                                   final String service) {
        notNull(server, "server must not be null");
        notNull(username, "username must not be null");
        notNull(password, "password must not be null");
        notNull(service, "service must not be null");

        return getServiceTicket(server, getTicketGrantingTicket(server, username, password), service);
    }


    /**
     * 第一步 获取ticket
     *
     * @param server
     * @param username
     * @param password
     */
    public static String getTicketGrantingTicket(final String server, final String username, final String password) {
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        System.out.println("第一步 getTicketGrantingTicket请求：" + server);
        String body = "";
        CloseableHttpResponse response = null;
        try {
            response = HttpUrlConnectionUtils.postResponse(server, map);
            int code = response.getStatusLine().getStatusCode();
            //获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "UTF-8");
            }
            EntityUtils.consume(entity);
            switch (code) {
                case 201:
                    final Matcher matcher = Pattern.compile(".*action=\".*/(.*?)\".*")
                            .matcher(body);
                    if (matcher.matches()) {
                        String result = matcher.group(1);
                        System.out.println("getTicketGrantingTicket结果：" + result);
                        return result;
                    }
                    break;
                default:
                    Log.error("ERROR: Response (1k):" + body.substring(0, Math.min(1024, body.length())));
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close(); //释放资源
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 第二步 取得ST
     *
     * @param server
     * @param ticketGrantingTicket
     * @param service
     */
    public static String getServiceTicket(final String server, final String ticketGrantingTicket, final String service) {
        if (ticketGrantingTicket == null)
            return null;
        String url = server + "/" + ticketGrantingTicket;
        System.out.println("第二步getServiceTicket请求：" + url);
        Map<String, String> map = new HashMap<>();
        map.put("service", service);
        String body = "";
        CloseableHttpResponse response = null;
        try {
            response = HttpUrlConnectionUtils.postResponse(url, map);
            int code = response.getStatusLine().getStatusCode();
            //获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "UTF-8");
            }
            EntityUtils.consume(entity);
            switch (code) {
                case 200:
                    return body;
                default:
                    Log.error("ERROR: Response (1k):" + body.substring(0, Math.min(1024, body.length())));
                    break;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close(); //释放资源
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 第三步
     * ticket校验
     *
     * @param serverValidate
     * @param serviceTicket
     * @param service
     */
    public static String ticketValidate(String serverValidate, String serviceTicket, String service) {
        notNull(serviceTicket, "paramter 'serviceTicket' is not null");
        notNull(service, "paramter 'service' is not null");
        String body = "";
        CloseableHttpResponse response = null;
        try {
            String url = serverValidate + "?ticket=" + serviceTicket + "&service=" + URLEncoder.encode(service, "UTF-8");
            System.out.println("第三步 ticketValidate请求：" + url);
            response = HttpUrlConnectionUtils.getResponse(url);
            int code = response.getStatusLine().getStatusCode();
            //获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "UTF-8");
            }
            switch (code) {
                case 200:
                    System.out.println("ticketValidate请求结果：" + body);
                    break;
                default:
                    Log.error("ERROR: Response (1k):" + body.substring(0, Math.min(1024, body.length())));
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close(); //释放资源
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return body;
    }

    public static void notNull(final Object object, final String message) {
        if (object == null)
            throw new IllegalArgumentException(message);
    }

    public static void main(final String[] args) throws Exception {
        final String casPath = "http://localhost:8080/cas"; //cache.config.get("casServer");
        final String server = casPath + "/v1/tickets";
        final String username = "fbdw";
        final String password = "123456";
        final String service = "http://localhost:8081/platform-main/";
        final String proxyValidate = casPath + "/proxyValidate";// proxyValidate
        String body = ticketValidate(proxyValidate, getTicket(server, username, password, service), service);
        System.out.println(body);
    }


}
