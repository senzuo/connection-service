package com.chh.ap.cs.dao;

import com.chh.ap.cs.client.SessionManager.SessionInfo;

public interface IDeviceDao {

	public SessionInfo login(String deviceId,int deviceType) throws Exception;
}
