package com.zjasm.util;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: daixx
 * Date: 16-7-20
 * Time: 上午8:44
 * To change this template use File | Settings | File Templates.
 */
public class ParameterUtil {

    /**
     * 对传入的字符串str进行Html encode转换
     */
    public static String escapeHtmlEncode(String str) {
        if(str ==null || str.trim().equals(""))   return str;
        String esStr  = StringEscapeUtils.escapeHtml(str);// 汉字会转换成对应的ASCII码，空格不转换
        return esStr;
    }

    private static String htmlCharEncode(char c) {
        switch(c) {
            case '&':
                return"&amp;";
            case '<':
                return"&lt;";
            case '>':
                return"&gt;";
            case '"':
                return"&quot;";
            /*case ' ':
                return"&nbsp;";*/
            default:
                return c +"";
        }
    }
    /** 对传入的字符串str进行Html encode转换 */
    public static String htmlEncode(String str) {
        if(str ==null || str.trim().equals(""))   return str;
        StringBuilder encodeStrBuilder = new StringBuilder();
        for (int i = 0, len = str.length(); i < len; i++) {
            encodeStrBuilder.append(htmlCharEncode(str.charAt(i)));
        }
        return encodeStrBuilder.toString();
    }

    /**
     * SQL注入
     * 比较通用的一个方法：（||之间的参数可以根据自己程序的需要添加）
     * 传入字符串存在SQL注入字符返回true，否则返回false
     * @param str
     * @return
     */
    public static boolean sqlInj(String str){
        str = str.toLowerCase();
        //String inj_str = "'|and|exec|insert|select|delete|update|count|*|%|chr|mid|master|truncate|char|declare|;|or|-|+|,";
        String inj_str = "'| and |exec |insert |select |delete |drop |update | count| chr | mid |master |truncate | char |declare | or | --|1=1";
        String[] inj_stra = inj_str.split("\\|");
        for (int i=0 ; i<inj_stra.length;i++ ){
            if (str.indexOf(inj_stra[i])!=-1){
                return true;
            }
        }
        return false;
    }


    /** * 判断是否为合法IP * @return the ip */
    public static boolean isboolIp(String ipAddress) {
        String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}.*";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    public static void main(String[] args){
       /* String inj_str = "'|and|exec|insert|select|delete|update|count|*|%|chr|mid|master|truncate|char|declare|;|or|-|+|,";
        String[] inj_stra = inj_str.split("\\|");
        for(int i=0;i<inj_stra.length;i++){
            System.out.println(inj_stra[i]);
        }*/
        System.out.println(isboolIp(""));
        System.out.println(isboolIp("192.168.1.1"));
        System.out.println(isboolIp("192.168.1.1:8080"));
        System.out.println(isboolIp("192.168.1.1:8080/client"));
        System.out.println(isboolIp("256.2.3.4"));
        System.out.println(isboolIp("1.2.3.4"));
        System.out.println(isboolIp("1.2.3.4.5"));
        System.out.println(isboolIp("1.2.3.4."));
    }


}
