package com.zjasm.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class AppliProUtil {

    private static AppliProUtil appliPro = null;

    private Properties properties;

    /**
     * 获取唯一实例
     * @return
     */
    public static AppliProUtil getInstance(){
       if(appliPro==null){
           synchronized (AppliProUtil.class){
               if(appliPro==null){
                   appliPro = new AppliProUtil();
               }
           }
       }
       return appliPro;
    }

    /**
     * 构造方法
     */
    private AppliProUtil(){
        properties = new Properties();
        try{
            InputStreamReader is = new InputStreamReader((InputStream) AppliProUtil.class.getClassLoader().getResource("application.properties").getContent(),"UTF-8");
            properties.load(is);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据key获取一个配置的值
     * @param key
     * @return
     */
    public  String getOneProp(String key){
        return properties.getProperty(key);
    }

}
