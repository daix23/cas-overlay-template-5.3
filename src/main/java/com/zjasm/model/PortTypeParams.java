package com.zjasm.model;

import com.zjasm.util.CommonUtil;
import com.zjasm.util.MD5Util;


/**
 * 调用易和接口 参数对象
 * 
 *  
 * @author syd
 * @version 1.0
 * @taskId 
 * @CreateDate 2016-4-21 
 * @since V2.0
 * @see zjzw.WebService.model
 */
public class PortTypeParams {

	public PortTypeParams() throws Exception{
	    this.curTimeStr =  CommonUtil.GetTimeString();
	    this.serviceCode= "zjdlxxggfw";//"servicecode";
	    this.servicepwd= "CustRBTU";//"servicepwd";
	    this.sign = MD5Util.GetMD5Code(serviceCode+servicepwd+curTimeStr);
		
	}
	
	private String serviceCode="";
	private String servicepwd="";
	private String sign;
	private String scope="1";
	private String datatype="json";
	private String curTimeStr="";
	private String encryptiontype="3";//密码加密方式 1. 表示明文    2. 表示Base64(UTF-8字符集操作)  3 .MD5 （标准32位小写） 4 .AES （sun自带库加密，密钥为SSO提供）5. 3DES （sun自带库加密，密钥为SSO提供）
	
	public String getCurTimeStr() {
		return curTimeStr;
	}

	public void setCurTimeStr(String curTimeStr) {
		this.curTimeStr = curTimeStr;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getServicepwd() {
		return servicepwd;
	}

	public void setServicepwd(String servicepwd) {
		this.servicepwd = servicepwd;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	/**
	 * Description: <br>
	 *  
	 * @author XXX<br>
	 * @taskId <br>
	 * @return encryptiontype <br>
	 */
	public String getEncryptiontype() {
		return encryptiontype;
	}

	/**
	 * Description: <br>
	 *  
	 * @author XXX<br>
	 * @taskId <br>
	 * @param encryptiontype <br>
	 */
	public void setEncryptiontype(String encryptiontype) {
		this.encryptiontype = encryptiontype;
	}
}
