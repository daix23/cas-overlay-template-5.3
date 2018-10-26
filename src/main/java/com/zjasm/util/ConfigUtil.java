package com.zjasm.util;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

/**
 */
public class ConfigUtil {
    private Properties props = null;
    private String configname;
    private static HashMap<String, ConfigUtil> configs = new HashMap<String, ConfigUtil>();

    private ConfigUtil() {

    }
    public int getCount(){
        if(props == null)
            return 0;
        return props.size();
    }
    public String getProperty(int i ){
        String[] keys = props.keySet().toArray(new String[0]);
        return getProperty(keys[i]);
    }
    public String[] getAllKeys(){
        return props.keySet().toArray(new String[0]);
    }
    public static ConfigUtil getConfig(String configFilename) {
        if (!configs.containsKey(configFilename)) {
            ConfigUtil config = new ConfigUtil();
            config.configname = configFilename;
            Properties prop = new Properties();
            InputStreamReader is;
            try {
                //is = new InputStreamReader(Config.class.getClassLoader().getResourceAsStream(configFilename), "UTF-8");
                is = new InputStreamReader((InputStream)ConfigUtil.class.getClassLoader().getResource(configFilename).getContent(),"UTF-8");
                prop.load(is);
                config.props = prop;
                is.close();
                configs.put(configFilename, config);
                return config;
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.getStackTrace();
            }
            return null;
        } else {
            return configs.get(configFilename);
        }
    }
    public void setProperty(String key,String value) throws FileNotFoundException,IOException{
        props.setProperty(key,value);
        InputStreamReader fileReader = new InputStreamReader(new FileInputStream(ConfigUtil.class.getClassLoader().getResource(configname).getFile()),"UTF-8");
        BufferedReader reader=new BufferedReader(fileReader);
        String line = reader.readLine();
        String content = "";
        boolean set = false;
        while(line != null){
            if(line.replace(" ", "").startsWith(key + "=")){
                line = key+"="+value;
                set = true;
            }
            content += line + "\n";
            line = reader.readLine();
        }
        if(!set){
            content += key+"="+value;
        }
        fileReader.close();
        reader.close();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ConfigUtil.class.getClassLoader().getResource(configname).getFile()),"UTF-8"));
        bufferedWriter.write(content);
        bufferedWriter.flush();
        bufferedWriter.close();
    }
    public static void refresh(String configfilename) {
        ConfigUtil config = getConfig(configfilename);
        if(config != null)
            config.props = null;
        configs.remove(configfilename);
    }
    public static void refresh(){
        configs.clear();
    }
    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public static void main(String[] args) {
        String authIdmFlag= ConfigUtil.getConfig("config.properties").getProperty("authIdmFlag");
        System.out.println(authIdmFlag);
    }

}
