package com.zjasm.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.nio.charset.Charset;

public class HttpPostJsonUtil {
    /**
     * post请求
     *
     * @param url
     * @param strJson
     * @return json object
     */
    public static JSONObject doPost(String url, String strJson) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        JSONObject response = null;
        post.addHeader("Content-Type","application/json;charset=UTF-8");

        try {
            //strJson = URLEncoder.encode(strJson, "UTF-8");
            //StringEntity s = new StringEntity(strJson);
            StringEntity s = new StringEntity(strJson, Charset.forName("UTF-8"));
            post.setEntity(s);
            HttpResponse res = client.execute(post);
            if (res.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = res.getEntity();
                String result = EntityUtils.toString(entity);
                response = JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public static void main(String[] args) {
        String str = "{" +
                "\"userid\": \"43439fjwrj32rjsd32423\",\"authlevel\": 2," +
                "\"idtype\": 1,\"username\": \"zjasm_你好\",\"idnum\": \"430215221202123022\"," +
                "\"passport\": \"\",\"permitlicense\": \"\",\"taiwanlicense\": \"\"," +
                "\"officerlicense\": \"\",\"greencard\": \"\",\"sex\": 1," +
                "\"nation\": \"汉族\",\"loginname\": \"zjasm_1\",\"email\": \"\"," +
                "\"mobile\": \"\",\"postcode\": \"\",\"cakey\": \"\",\"birthday\": \"\"" +
                "}";

        //String url ="http://118.178.118.177:8080/RSComplat/rest/Personal/User/addUser";
        String url ="http://126.33.10.81:8869/RSComplat/rest/Personal/User/addUser";
        JSONObject res = doPost(url,str);
        int code = (Integer)res.get("code");
        System.out.println(code);

    }


}
