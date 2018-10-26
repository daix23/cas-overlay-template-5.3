package com.zjasm.model;

public class ResponseObj<T> {
	public int page;
	public int code;
	public T result;
	public int count;
	public int index;
	public int size;
	public String message;

	public ResponseObj(int _code, T _result) {
		code = _code;
		result = _result;
		message = "无";
	}

	public ResponseObj(int _code, String _message) {
		code = _code;
		message = _message;
	}

	public ResponseObj(int _code, T _result, String _message) {
		code = _code;
		result = _result;
		message = _message;
	}
	
	public ResponseObj(int _code, T _result, int _count) {
		code = _code;
		result = _result;
		count = _count;
	}
	
	public ResponseObj(int _page, T _result, int _count, int _size) {
		page = _page;
		result = _result;
		count = _count;
		size = _size;
	}

	/**
	 * Description: <br>
	 *  
	 * @author XXX<br>
	 * @taskId <br>
	 * @return code <br>
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Description: <br>
	 *  
	 * @author XXX<br>
	 * @taskId <br>
	 * @param code <br>
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * Description: <br>
	 *  
	 * @author XXX<br>
	 * @taskId <br>
	 * @return result <br>
	 */
	public T getResult() {
		return result;
	}

	/**
	 * Description: <br>
	 *  
	 * @author XXX<br>
	 * @taskId <br>
	 * @param result <br>
	 */
	public void setResult(T result) {
		this.result = result;
	}

	/**
	 * Description: <br>
	 *  
	 * @author XXX<br>
	 * @taskId <br>
	 * @return message <br>
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Description: <br>
	 *  
	 * @author XXX<br>
	 * @taskId <br>
	 * @param message <br>
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
