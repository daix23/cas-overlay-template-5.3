package com.zjasm.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertiesLoaderUtil {

    private static PropertiesLoaderUtil m_propertiesLoader = null;

    private Properties properties;

    /**
     * 获取唯一实例
     * @return
     */
    public static PropertiesLoaderUtil getInstance(){
       if(m_propertiesLoader==null){
           synchronized (PropertiesLoaderUtil.class){
               if(m_propertiesLoader==null){
                   m_propertiesLoader = new PropertiesLoaderUtil();
               }
           }
       }
       return m_propertiesLoader;
    }

    /**
     * 构造方法
     */
    private PropertiesLoaderUtil(){
        properties = new Properties();
        try{
            InputStreamReader is = new InputStreamReader((InputStream)PropertiesLoaderUtil.class.getClassLoader().getResource("config.properties").getContent(),"UTF-8");
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
