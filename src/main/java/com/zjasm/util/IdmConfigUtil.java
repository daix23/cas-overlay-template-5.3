package com.zjasm.util;

import com.commnetsoft.IDMConfig;

public class IdmConfigUtil {

    private static IdmConfigUtil m_idmConfigUtil = null;

    public final static String IDM_KEY_GOV = "idm_gov";

    /**
     * 获取唯一实例
     * @return
     */
    public static IdmConfigUtil getInstance(){
        if(m_idmConfigUtil==null){
            synchronized (IdmConfigUtil.class){
                if(m_idmConfigUtil==null){
                    m_idmConfigUtil = new IdmConfigUtil();
                }
            }
        }
        return m_idmConfigUtil;
    }

    /**
     * 构造方法
     */
    private IdmConfigUtil(){
        PropertiesLoaderUtil propertiesLoaderUtil = PropertiesLoaderUtil.getInstance();
        try{
            String idmUrl = propertiesLoaderUtil.getOneProp("idmUrl");
            String servicecode = propertiesLoaderUtil.getOneProp("servicecode");
            String servicepwd = propertiesLoaderUtil.getOneProp("servicepwd");
            IDMConfig.createProp(IDM_KEY_GOV, idmUrl, servicecode, servicepwd);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
