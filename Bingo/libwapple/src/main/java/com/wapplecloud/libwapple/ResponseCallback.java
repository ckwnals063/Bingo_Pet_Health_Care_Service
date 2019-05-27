package com.wapplecloud.libwapple;

import org.json.JSONObject;

public abstract class ResponseCallback {
	public abstract void onError(final int errorCode, final String errorMsg);
	
	public abstract void onDataReceived(final JSONObject jsonResponse);

	public abstract void onReceiveResponse(); // 에러든 정상이든 응답 받았을 때 

	public void onBinaryDataReceived(String filePath) {
		
	}
}
