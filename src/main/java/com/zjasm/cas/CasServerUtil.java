package com.zjasm.cas;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * 首先客户端提交用户名、密码、及Service三个参数，
 * 如果验证成功便返回用户的TGT(Ticket Granting Ticket)至客户端,
 * 然后客户端再根据 TGT 获取用户的 ST(Service Ticket)来进行验证登录。
 * 故名思意，TGT是用于生成一个新的Ticket(ST)的Ticket，
 * 而ST则是提供给客户端用于登录的Ticket，两者最大的区别在于，
 * TGT是用户名密码验证成功之后所生成的Ticket，并且会保存在Server中及Cookie中，
 * 而ST则必须是是根据TGT来生成，主要用于登录，并且当登录成功之后 ST 则会失效。
 */
public class CasServerUtil {

    // 登录服务器地址
    private static final String CAS_SERVER_PATH = "http://126.33.10.60:8443";

    // 登录地址的token
    private static final String GET_TOKEN_URL = CAS_SERVER_PATH + "/v1/tickets";


    public static void main(String[] args) {

        try {
            String tgt = getTGT("superadmin", "123456");
            System.out.println("TGT：" + tgt);

            String service = "http://126.33.50.237:8809/cas-management/manage.html";
            String st = getST(tgt, service);
            System.out.println("ST：" + st);

            System.out.println(service + "?ticket=" + st);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取TGT
     */
    public static String getTGT(String username, String password) {
        try{
            CookieStore httpCookieStore = new BasicCookieStore();
//        CloseableHttpClient client = createHttpClientWithNoSsl(httpCookieStore);

            CloseableHttpClient client = HttpClients.createDefault();

            HttpPost httpPost = new HttpPost(GET_TOKEN_URL);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = client.execute(httpPost);

//        System.out.println("\n 获取TGT，Header响应");
//        Header[] allHeaders = response.getAllHeaders();
//        for (int i = 0; i < allHeaders.length; i++) {
//            System.out.println("Key：" + allHeaders[i].getName() + "，Value：" + allHeaders[i].getValue() + "，Elements:" + Arrays.toString(allHeaders[i].getElements()));
//        }

            Header headerLocation = response.getFirstHeader("Location");
            String location = headerLocation == null ? null : headerLocation.getValue();

            System.out.println("Location：" + location);

            if (location != null) {
                return location.substring(location.lastIndexOf("/") + 1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
//
        return null;
    }


    /**
     * 获取ST
     */
    public static String getST(String TGT, String service){
        try {

//        CookieStore httpCookieStore = new BasicCookieStore();
//        CloseableHttpClient client = createHttpClientWithNoSsl(httpCookieStore);

            CloseableHttpClient client = HttpClients.createDefault();


            // service 需要encoder编码
            // service = URLEncoder.encode(service, "utf-8");

            HttpPost httpPost = new HttpPost(GET_TOKEN_URL + "/" + TGT);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("service", service));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = client.execute(httpPost);

//        System.out.println("\n 获取ST，Header响应");
//        Header[] allHeaders = response.getAllHeaders();
//        for (int i = 0; i < allHeaders.length; i++) {
//            System.out.println("Key：" + allHeaders[i].getName() + "，Value：" + allHeaders[i].getValue() + "，Elements:" + Arrays.toString(allHeaders[i].getElements()));
//        }
//
//
//        List<Cookie> cookies = httpCookieStore.getCookies();
//        System.out.println("Cookie.size：" + cookies.size());
//        for (Cookie cookie : cookies) {
//            System.out.println("Cookie: " + new Gson().toJson(cookie));
//        }

            String st = readResponse(response);
            return st == null ? null : (st == "" ? null : st);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 读取 response body 内容为字符串
     *
     * @param response
     * @return
     * @throws IOException
     */
    private static String readResponse(HttpResponse response) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String result = new String();
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        return result;
    }


    /**
     * 创建模拟客户端（针对 https 客户端禁用 SSL 验证）
     *
     * @param cookieStore 缓存的 Cookies 信息
     * @return
     * @throws Exception
     */
    private static CloseableHttpClient createHttpClientWithNoSsl(CookieStore cookieStore) throws Exception {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // don't check
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // don't check
                    }
                }
        };

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAllCerts, null);
        LayeredConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(ctx);
        return HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .setDefaultCookieStore(cookieStore == null ? new BasicCookieStore() : cookieStore)
                .build();
    }

}